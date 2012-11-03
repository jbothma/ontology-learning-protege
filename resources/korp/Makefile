corpus = ProtegeOLPlugin
original_dir = original
files := $(basename $(notdir $(wildcard $(original_dir)/*.xml)))

vrt_annotations = word pos msd baseform ref dephead.ref deprel sentence.n paragraph.n sentence.id n
vrt_columns     = word pos msd lemma    ref dephead     deprel
vrt_structs     = -    -   -   -        -   -           -      sentence:n paragraph:n sentence:id

xml_elements    = body p
xml_annotations = body paragraph
xml_skip =

include $(KORP_INSTALL)/corpora/Makefile.common

token_chunk = sentence
token_segmenter = punkt_word

sentence_chunk = paragraph
sentence_segmenter = punkt_sentence
sentence_model = $(punkt_model)

paragraph_chunk = body
paragraph_segmenter = blanklines

################################################################################
#                                  RULES                                       #
################################################################################

include $(KORP_INSTALL)/corpora/Makefile.rules
