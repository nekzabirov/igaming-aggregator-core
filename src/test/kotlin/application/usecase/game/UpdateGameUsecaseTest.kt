package application.usecase.game

import domain.common.error.NotFoundError
import domain.game.model.Game
import domain.game.repository.GameRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UpdateGameUsecaseTest {

    private val gameRepository: GameRepository = mockk()
    private val usecase = UpdateGameUsecase(gameRepository)

    @Test
    fun `invoke updates game successfully`() = runTest {
        val gameId = UUID.randomUUID()
        val providerId = UUID.randomUUID()
        val existingGame = Game(
            id = gameId,
            identity = "test-game",
            name = "Test Game",
            providerId = providerId,
            active = true,
            bonusBetEnable = true,
            bonusWageringEnable = true
        )

        val command = UpdateGameCommand(
            identity = "test-game",
            name = "Updated Game",
            active = false
        )

        val updatedGameSlot = slot<Game>()

        coEvery { gameRepository.findByIdentity("test-game") } returns existingGame
        coEvery { gameRepository.update(capture(updatedGameSlot)) } answers { updatedGameSlot.captured }

        val result = usecase(command)

        assertTrue(result.isSuccess)
        val updatedGame = result.getOrThrow()
        assertEquals("Updated Game", updatedGame.name)
        assertEquals(false, updatedGame.active)
        coVerify(exactly = 1) { gameRepository.update(any()) }
    }

    @Test
    fun `invoke returns failure when game not found`() = runTest {
        val command = UpdateGameCommand(
            identity = "non-existent-game",
            name = "Updated Name"
        )

        coEvery { gameRepository.findByIdentity("non-existent-game") } returns null

        val result = usecase(command)

        assertTrue(result.isFailure)
        assertIs<NotFoundError>(result.exceptionOrNull())
        coVerify(exactly = 0) { gameRepository.update(any()) }
    }

    @Test
    fun `invoke preserves unchanged fields`() = runTest {
        val gameId = UUID.randomUUID()
        val providerId = UUID.randomUUID()
        val existingGame = Game(
            id = gameId,
            identity = "test-game",
            name = "Original Name",
            providerId = providerId,
            active = true,
            bonusBetEnable = true,
            bonusWageringEnable = false
        )

        val command = UpdateGameCommand(
            identity = "test-game",
            active = false
        )

        val updatedGameSlot = slot<Game>()

        coEvery { gameRepository.findByIdentity("test-game") } returns existingGame
        coEvery { gameRepository.update(capture(updatedGameSlot)) } answers { updatedGameSlot.captured }

        val result = usecase(command)

        assertTrue(result.isSuccess)
        val updatedGame = result.getOrThrow()
        assertEquals("Original Name", updatedGame.name)
        assertEquals(false, updatedGame.active)
        assertEquals(true, updatedGame.bonusBetEnable)
        assertEquals(false, updatedGame.bonusWageringEnable)
    }

    @Test
    fun `invoke with convenience method works correctly`() = runTest {
        val gameId = UUID.randomUUID()
        val providerId = UUID.randomUUID()
        val existingGame = Game(
            id = gameId,
            identity = "test-game",
            name = "Test Game",
            providerId = providerId,
            active = true,
            bonusBetEnable = false
        )

        val updatedGameSlot = slot<Game>()

        coEvery { gameRepository.findByIdentity("test-game") } returns existingGame
        coEvery { gameRepository.update(capture(updatedGameSlot)) } answers { updatedGameSlot.captured }

        val result = usecase(identity = "test-game", active = false, bonusBet = true)

        assertTrue(result.isSuccess)
        val updatedGame = result.getOrThrow()
        assertEquals(false, updatedGame.active)
        assertEquals(true, updatedGame.bonusBetEnable)
    }
}

class AddGameTagUsecaseTest {

    private val gameRepository: GameRepository = mockk()
    private val usecase = AddGameTagUsecase(gameRepository)

    @Test
    fun `invoke adds tag successfully`() = runTest {
        val gameId = UUID.randomUUID()
        val game = Game(
            id = gameId,
            identity = "test-game",
            name = "Test Game",
            providerId = UUID.randomUUID()
        )

        coEvery { gameRepository.findByIdentity("test-game") } returns game
        coEvery { gameRepository.addTag(gameId, "new-tag") } returns true

        val result = usecase("test-game", "new-tag")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { gameRepository.addTag(gameId, "new-tag") }
    }

    @Test
    fun `invoke returns failure when game not found`() = runTest {
        coEvery { gameRepository.findByIdentity("non-existent") } returns null

        val result = usecase("non-existent", "tag")

        assertTrue(result.isFailure)
        assertIs<NotFoundError>(result.exceptionOrNull())
    }

    @Test
    fun `invoke returns failure when addTag fails`() = runTest {
        val gameId = UUID.randomUUID()
        val game = Game(
            id = gameId,
            identity = "test-game",
            name = "Test Game",
            providerId = UUID.randomUUID()
        )

        coEvery { gameRepository.findByIdentity("test-game") } returns game
        coEvery { gameRepository.addTag(gameId, "tag") } returns false

        val result = usecase("test-game", "tag")

        assertTrue(result.isFailure)
    }
}

class RemoveGameTagUsecaseTest {

    private val gameRepository: GameRepository = mockk()
    private val usecase = RemoveGameTagUsecase(gameRepository)

    @Test
    fun `invoke removes tag successfully`() = runTest {
        val gameId = UUID.randomUUID()
        val game = Game(
            id = gameId,
            identity = "test-game",
            name = "Test Game",
            providerId = UUID.randomUUID(),
            tags = listOf("existing-tag")
        )

        coEvery { gameRepository.findByIdentity("test-game") } returns game
        coEvery { gameRepository.removeTag(gameId, "existing-tag") } returns true

        val result = usecase("test-game", "existing-tag")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { gameRepository.removeTag(gameId, "existing-tag") }
    }

    @Test
    fun `invoke returns failure when game not found`() = runTest {
        coEvery { gameRepository.findByIdentity("non-existent") } returns null

        val result = usecase("non-existent", "tag")

        assertTrue(result.isFailure)
        assertIs<NotFoundError>(result.exceptionOrNull())
    }
}
