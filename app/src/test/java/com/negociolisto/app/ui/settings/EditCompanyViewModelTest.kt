package com.negociolisto.app.ui.settings

import android.net.Uri
import com.negociolisto.app.domain.model.User
import com.negociolisto.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeAuthRepo(initial: User?) : AuthRepository {
    private val state = MutableStateFlow(initial)
    override val currentUser: Flow<User?> = state.asStateFlow()
    override val isAuthenticated: Flow<Boolean> get() = TODO()
    override suspend fun register(email: String, password: String, name: String, phone: String?, businessName: String?) = TODO()
    override suspend fun login(email: String, password: String) = TODO()
    override suspend fun logout() {}
    override suspend fun sendPasswordResetEmail(email: String) = TODO()
    override suspend fun updateProfile(user: User): Result<User> {
        state.value = user
        return Result.success(user)
    }
}

class EditCompanyViewModelTest {
    @Test
    fun `guarda logo y descripcion`() = runBlocking {
        val user = User(
            id = "u1", name = "Test", email = "t@t.com", phone = null,
            businessName = null, businessType = null, businessRut = null,
            businessAddress = null, businessPhone = null, businessEmail = null,
            businessSocialMedia = null, businessLogoUrl = null, profilePhotoUrl = null,
            isEmailVerified = false, createdAt = null, updatedAt = null, lastLoginAt = null,
            isCloudSyncEnabled = false, preferences = com.negociolisto.app.domain.model.UserPreferences()
        )
        val repo = FakeAuthRepo(user)
        val vm = EditCompanyViewModel(repo)

        vm.updateBusinessName("Mi Empresa")
        vm.updateBusinessDescription("Somos los mejores")
        vm.selectLogo(Uri.parse("content://images/logo.png"))

        vm.saveCompany(androidx.test.core.app.ApplicationProvider.getApplicationContext())

        val saved = (repo.currentUser as MutableStateFlow<User?>).value!!
        assertEquals("Mi Empresa", saved.businessName)
        assertEquals("Somos los mejores", saved.businessDescription)
        assertEquals("content://images/logo.png", saved.businessLogoUrl)
    }
}


