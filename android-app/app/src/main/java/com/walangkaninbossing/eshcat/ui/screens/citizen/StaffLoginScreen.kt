package com.walangkaninbossing.eshcat.ui.screens.citizen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.PrimaryButton
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.SecondaryButton
import com.walangkaninbossing.eshcat.ui.theme.EshcatRadius
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.PrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.TextSecondaryLight
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel

@Composable
fun StaffLoginScreen(nav: NavController, sessionVm: StaffSessionViewModel) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val state by sessionVm.state.collectAsState()

    val performLogin: (String, String) -> Unit = { userToLogin, passToLogin ->
        username = userToLogin
        password = passToLogin
        sessionVm.login(userToLogin, passToLogin) { error ->
            if (error == null) {
                Toast.makeText(context, "Welcome back", Toast.LENGTH_SHORT).show()
                nav.navigate(Routes.STAFF_DASHBOARD) {
                    popUpTo(Routes.STAFF_LOGIN) { inclusive = true }
                }
            } else {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val signIn: () -> Unit = {
        performLogin(username, password)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Staff Login", onBack = { nav.popBackStack() })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding, vertical = EshcatSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md),
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainerLight),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.height(EshcatSpacing.md))
                    Text(
                        text = "Staff Portal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Sign in to manage services and applications",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(EshcatRadius.md),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Next,
                ),
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(EshcatRadius.md),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { signIn() }),
            )

            PrimaryButton(
                text = "Sign In",
                enabled = username.isNotBlank() && password.isNotBlank() && !state.loading,
                loading = state.loading,
                onClick = signIn,
            )

            val loginUser = state.user
            if (loginUser != null) {
                SecondaryButton(
                    text = "Continue as ${loginUser.fullName}",
                    onClick = { nav.navigate(Routes.STAFF_DASHBOARD) },
                )
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Demo Accounts (One-Tap Sign In)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondaryLight,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tap any demo account to auto-fill credentials and sign in directly:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))

                val demoAccounts = listOf(
                    "superadmin" to "Super Admin",
                    "admin" to "Admin",
                    "pedro.reyes" to "Dept Head",
                    "carmen.lopez" to "Staff (Carmen)",
                    "juan.delacruz" to "Staff (Juan)",
                    "ana.cruz" to "Auditor"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(demoAccounts) { (demoUser, roleLabel) ->
                        SuggestionChip(
                            onClick = { performLogin(demoUser, "123456") },
                            label = { Text(roleLabel) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            enabled = !state.loading
                        )
                    }
                }
            }
        }
    }
}