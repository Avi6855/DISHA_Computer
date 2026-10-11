package com.avinashpatil.app.dishacomputer.data.converters

import com.avinashpatil.app.dishacomputer.api.models.BatchDto
import com.avinashpatil.app.dishacomputer.api.models.QuizQuestionDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Room stores lists as JSON through these converters. If a round trip breaks, cached courses,
 * batches or quiz questions come back empty after a restart — so each type is covered here.
 */
class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `string list round trips`() {
        val list = listOf("CCC", "Android")

        val json = converters.toStringList(list)

        assertEquals(list, converters.fromStringList(json))
    }

    @Test
    fun `batch dto list round trips with all fields`() {
        val batch = BatchDto(
            batchId = "batch-1",
            batchNo = "07",
            startDate = "2026-01-05",
            endDate = "2026-06-05",
            timing = "10:00 AM - 12:00 PM",
            courseId = "course-1",
            studentIds = listOf("user-1", "user-2")
        )

        val json = converters.toBatchDtoList(listOf(batch))
        val restored = converters.fromBatchDtoList(json)

        assertEquals(listOf(batch), restored)
    }

    @Test
    fun `quiz question dto list round trips`() {
        val question = QuizQuestionDTO(
            quizQuestionId = "qq-1",
            quizId = "quiz-1",
            questionId = "question-1"
        )

        val json = converters.toQuizQuestionDTOList(listOf(question))

        assertEquals(listOf(question), converters.fromQuizQuestionDTOList(json))
    }

    @Test
    fun `empty lists survive the round trip`() {
        assertEquals(emptyList<String>(), converters.fromStringList(converters.toStringList(emptyList())))
        assertEquals(emptyList<BatchDto>(), converters.fromBatchDtoList(converters.toBatchDtoList(emptyList())))
        assertEquals(
            emptyList<QuizQuestionDTO>(),
            converters.fromQuizQuestionDTOList(converters.toQuizQuestionDTOList(emptyList()))
        )
    }

    @Test
    fun `null values stay null instead of crashing`() {
        assertNull(converters.fromStringList(null))
        assertNull(converters.toStringList(null))
        assertNull(converters.fromBatchDtoList(null))
        assertNull(converters.toBatchDtoList(null))
        assertNull(converters.fromQuizQuestionDTOList(null))
        assertNull(converters.toQuizQuestionDTOList(null))
    }
}
