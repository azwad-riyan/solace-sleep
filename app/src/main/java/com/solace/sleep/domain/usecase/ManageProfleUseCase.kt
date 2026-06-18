package com.solace.sleep.domain.usecase

import com.solace.sleep.data.repository.ProfileRepository
import com.solace.sleep.domain.model.Profile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    fun observeAll(): Flow<List<Profile>> = repository.observeAllProfiles()

    fun observeActive(): Flow<Profile?> = repository.observeActiveProfile()

    suspend fun create(name: String, emoji: String): Profile =
        repository.createDefaultProfile(name, emoji)

    suspend fun save(profile: Profile) = repository.saveProfile(profile)

    suspend fun update(profile: Profile) = repository.updateProfile(profile)

    suspend fun delete(profileId: String) = repository.deleteProfile(profileId)

    suspend fun switchTo(profileId: String) = repository.setActiveProfile(profileId)
}
