package me.yuji.glanceplayground.data

private val vocabularyList = listOf(
    "apple",
    "banana",
    "cherry",
    "date",
    "elderberry",
    "fig",
    "grape",
    "honeydew",
    "kiwi",
    "lemon",
    "mango",
    "nectarine",
    "orange",
    "papaya",
    "quince",
    "raspberry",
    "strawberry",
    "tangerine",
    "ugli",
    "vanilla",
    "watermelon",
    "xigua",
    "yuzu",
    "zucchini",
)

interface VocabularyRepository {
    suspend fun getAll(): List<Vocabulary>
}

class DummyVocabularyRepository : VocabularyRepository {

    private val learnedVocabularies = vocabularyList
        .filterIndexed { index, _ -> index % 2 == 0 }
        .toSet()

    override suspend fun getAll(): List<Vocabulary> {
        return vocabularyList.map { Vocabulary(it, it in learnedVocabularies) }
    }
}
