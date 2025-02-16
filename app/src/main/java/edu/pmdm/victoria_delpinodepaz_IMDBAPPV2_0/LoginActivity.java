package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote.FirestoreManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;

//Actividad encargada de gestionar el proceso de inicio de sesión con Google mediante Firebase.
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    //ActivityResultLauncher se encarga de manejar el resultado del intento de inicio de sesión.
    private final ActivityResultLauncher<IntentSenderRequest> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        // Obtiene las credenciales devueltas por Google
                        SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                        String idToken = credential.getGoogleIdToken();
                        if (idToken != null) {
                            // Inicia sesión en Firebase con el token recibido
                            firebaseAuthWithGoogle(idToken);
                        }
                    } catch (ApiException e) {
                        Log.e(TAG, "Error al obtener las credenciales de Google", e);
                    }
                } else {
                    Log.w(TAG, "Inicio de sesión cancelado o fallido\n\n"+result.getResultCode()+"\n"+result.getData().toString()+"\n");
                }
            });

    /* Método que se ejecuta al crear la actividad. Inicializa Firebase, configura One Tap Sign-In y
    establece la acción del botón de inicio de sesión.*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configura el cliente de inicio de sesión One Tap de Google
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(getString(R.string.default_web_client_id))
                                .setFilterByAuthorizedAccounts(false)
                                .build())
                .build();

        // Asigna el botón de inicio de sesión y define su comportamiento
        Button signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(v -> startSignIn());
    }

    //Inicia el proceso de autenticación con Google mediante One Tap Sign-In.
    private void startSignIn() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    try {
                        // Obtiene la solicitud de IntentSender para iniciar el flujo de inicio de sesión
                        IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                        signInLauncher.launch(intentSenderRequest);
                    } catch (Exception e) {
                        Log.e(TAG, "Error al iniciar el IntentSender", e);
                    }
                })
                .addOnFailureListener(this, e -> Log.e(TAG, "One Tap Sign-In Error: " + e.getMessage()));
    }

    //Autentica al usuario en Firebase con el token de Google obtenido.
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Obtiene el usuario autenticado
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "signInWithCredential:success, User: " + user.getDisplayName());
                        updateUI(user);
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        updateUI(null);
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    //Actualiza la interfaz de usuario dependiendo de si el usuario está autenticado o no.
    private void updateUI(FirebaseUser user) {
        if (user != null) {
                FirestoreManager.getUser(user.getEmail(), resultUser ->{
                    if (resultUser != null) {
                        AppPersistance.user = resultUser;
                        Log.d("AppPersis",AppPersistance.user.getEmail().toString());
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }
                    else {
                        FirestoreManager.createUser(result ->{
                            if (result ) {
                                if (user  != null) {
                                    FirestoreManager.getUser(user.getEmail(),resultNewUser ->{
                                        if (resultNewUser != null) {
                                            AppPersistance.user = resultNewUser;
                                            Log.d("AppPersis",AppPersistance.user.getEmail().toString());
                                            Intent intent = new Intent(this, MainActivity.class);
                                            startActivity(intent);
                                            finishAffinity();
                                        } else {
                                            Log.e("FirestoreError", "No se pudo obtener el usuario desde Firestore");
                                        }
                                    });
                                }
                            } else {
                                Log.d("CREATE_USER","error al crear un nuevo usuario");
                            }
                        });
                        }
                });



          /*
            //create user
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finishAffinity();*/
        } else {
            Log.d(TAG, "Usuario no autenticado");
        }
    }
}
