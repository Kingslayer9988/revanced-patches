package app.revanced.patches.facebook.ads.mainfeed

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import baseModelMapperFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction31i
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import getSponsoredDataModelTemplateFingerprint
import getStoryVisibilityFingerprint

@Suppress("unused")
val hideSponsoredStoriesPatch = bytecodePatch(
    name = "Hide 'Sponsored Stories'",
) {
    compatibleWith("com.facebook.katana")

    val getStoryVisibilityMatch by getStoryVisibilityFingerprint()
    val getSponsoredDataModelTemplateMatch by getSponsoredDataModelTemplateFingerprint()
    val baseModelMapperMatch by baseModelMapperFingerprint()

    execute {
        val sponsoredDataModelTemplateMethod = getSponsoredDataModelTemplateMatch.method
        val baseModelMapperMethod = baseModelMapperMatch.method
        val baseModelWithTreeType = baseModelMapperMethod.returnType

        val graphQlStoryClassDescriptor = "Lcom/facebook/graphql/model/GraphQLStory;"

        // The "SponsoredDataModelTemplate" methods has the ids in its body to extract sponsored data
        // from GraphQL models, but targets the wrong derived type of "BaseModelWithTree". Since those ids
        // could change in future version, we need to extract them and call the base implementation directly.
        val getSponsoredDataHelperMethod = ImmutableMethod(
            getStoryVisibilityMatch.classDef.type,
            "getSponsoredData",
            listOf(ImmutableMethodParameter(graphQlStoryClassDescriptor, null, null)),
            baseModelWithTreeType,
            AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
            null,
            null,
            MutableMethodImplementation(4),
        ).toMutable().apply {
            // Extract the ids of the original method. These ids seem to correspond to model types for
            // GraphQL data structure. They are then fed to a method of BaseModelWithTree that populate
            // and cast the requested GraphQL subtype. The Ids are found in the two first "CONST" instructions.
            val constInstructions = sponsoredDataModelTemplateMethod.implementation!!.instructions
                .asSequence()
                .filterIsInstance<Instruction31i>()
                .take(2)
                .toList()

            val storyTypeId = constInstructions[0].narrowLiteral
            val sponsoredDataTypeId = constInstructions[1].narrowLiteral

            addInstructions(
                """ 
                        const-class v2, $baseModelWithTreeType
                        const v1, $storyTypeId
                        const v0, $sponsoredDataTypeId
                        invoke-virtual {p0, v2, v1, v0}, $baseModelMapperMethod
                        move-result-object v0
                        check-cast v0, $baseModelWithTreeType
                        return-object v0
                    """,
            )
        }

        getStoryVisibilityMatch.mutableClass.methods.add(getSponsoredDataHelperMethod)

        // Check if the parameter type is GraphQLStory and if sponsoredDataModelGetter returns a non-null value.
        // If so, hide the story by setting the visibility to StoryVisibility.GONE.
        getStoryVisibilityMatch.mutableMethod.addInstructionsWithLabels(
            getStoryVisibilityMatch.patternMatch!!.startIndex,
            """
                    instance-of v0, p0, $graphQlStoryClassDescriptor
                    if-eqz v0, :resume_normal
                    invoke-static {p0}, $getSponsoredDataHelperMethod
                    move-result-object v0 
                    if-eqz v0, :resume_normal
                    const-string v0, "GONE"
                    return-object v0
                    :resume_normal
                    nop
                """,
        )
    }
}