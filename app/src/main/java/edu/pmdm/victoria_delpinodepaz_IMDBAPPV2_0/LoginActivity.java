package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Local.DBManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote.FirebaseAuthManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote.FirestoreManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;

//Actividad encargada de gestionar el proceso de inicio de sesión
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private CallbackManager callbackManager;

    //Maneja el resultado del intento de inicio de sesión
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configura el cliente de inicio de sesión One Tap de Google
        oneTapClient = Identity.getSignInClient(this);
        callbackManager = CallbackManager.Factory.create();

        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(getString(R.string.default_web_client_id))
                                .setFilterByAuthorizedAccounts(false)
                                .build())
                .build();

        EditText editTextEmail=findViewById(R.id.eTxtEmail);

        Button btnRegisterEmail=findViewById(R.id.btnRegisterEmail);

        //Registro de un nuevo usuario con email y contraseña
        btnRegisterEmail.setOnClickListener(v->{
            //Obtiene el email y la contraseña ingresados por el usuario
            EditText eTxtEmail=findViewById(R.id.eTxtEmail);
            String email= eTxtEmail.getText().toString();
            EditText eTxtPassword=findViewById(R.id.eTxtPassword);
            String password= eTxtPassword.getText().toString();
            FirebaseAuthManager.register(this, email, password);
        });

        Button btnLoginEmail=findViewById(R.id.btnLoginEmail);
        //Login con usuario y contraseña
        btnLoginEmail.setOnClickListener(v -> {
            //Obtiene el email y la contraseña para iniciar sesión con Firebase
            EditText eTxtEmail = findViewById(R.id.eTxtEmail);
            String email = eTxtEmail.getText().toString();
            EditText eTxtPassword = findViewById(R.id.eTxtPassword);
            String password = eTxtPassword.getText().toString();

            //Verifica si los datos son correctos
            FirebaseAuthManager.login(this, email, password, new FirebaseAuthManager.OnLoginListener() {
                @Override
                public void onSuccess() {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        updateUI(user);
                    } else {
                        Toast.makeText(getApplicationContext(), "Error: No se pudo obtener el usuario", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(getApplicationContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Validación de email en tiempo real
        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    editTextEmail.setError("Correo electrónico inválido");
                    btnLoginEmail.setEnabled(false);
                    btnRegisterEmail.setEnabled(false);
                } else {
                    editTextEmail.setError(null);
                    btnLoginEmail.setEnabled(true);
                    btnRegisterEmail.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        LoginButton fbLogingBnt= findViewById(R.id.login_button);
        // Configura el botón de inicio de sesión con Facebook
        fbLogingBnt.setPermissions("email","public_profile");
        fbLogingBnt.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FacebookLogin", "LoginResult recibido: " + loginResult.getAccessToken().getToken());
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("FacebookLogin", "Inicio de sesión cancelado");
            }

            @Override
            public void onError(@NonNull FacebookException e) {
                Log.e("FacebookLogin", "Error en el inicio de sesión", e);
            }
        });




        // Asigna el botón de inicio de sesión de Google y define su comportamiento
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

    // Autentica el usuario en Firebase con el token de Facebook
    private void firebaseAuthWithFacebook(AccessToken token) {
        Log.d("FacebookToken", "Token recibido: " + token.getToken()); // 🔹 Agrega este log para verificar el token
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Obtiene el usuario autenticado
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        Log.d("FacebookLogin", "Inicio de sesión exitoso con Facebook, Usuario: " + user.getDisplayName());
                        updateUI(user);
                    } else {
                        Log.e("FacebookLogin", "Error al iniciar sesión con Facebook", task.getException());
                        Toast.makeText(getApplicationContext(), "Error de autenticación con Facebook", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            for (UserInfo profile : currentUser.getProviderData()) {
                if (profile.getProviderId().equals(FacebookAuthProvider.PROVIDER_ID)) {
                    AccessToken accessToken = AccessToken.getCurrentAccessToken();
                    if (accessToken != null && !accessToken.isExpired()) {
                        firebaseAuthWithFacebook(accessToken);
                    }
                }
            }
            updateUI(currentUser);
        }
    }

    //Actualiza la interfaz de usuario dependiendo de si el usuario está autenticado o no.
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            //Comprueba si esta en firestore
                FirestoreManager.getUser(user.getEmail(), resultUser ->{
                    if (resultUser != null) {
                        //Asigna al usuario y comprueba si existe en la base de datos local
                        AppPersistance.user = DBManager.getOrCreateUser(this,user, resultUser.getUser_id());
                        Log.d("AppPersis",AppPersistance.user.getEmail().toString());
                        //Abre la actividad principal
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }
                    else {
                        //Si no existe crea el usuario en firestore
                        FirestoreManager.createUser(result ->{
                            if (result ) {
                                if (user  != null) {
                                    FirestoreManager.getUser(user.getEmail(),resultNewUser ->{
                                        if (resultNewUser != null) {
                                            //Asigna al usuario y comprueba si existe en la base de datos local
                                            AppPersistance.user = DBManager.getOrCreateUser(this,user,resultNewUser.getUser_id());
                                            Log.d("AppPersis",AppPersistance.user.getEmail().toString());
                                            //Abre actividad principal
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

        } else {
            Log.d(TAG, "Usuario no autenticado");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("FacebookLogin", "onActivityResult ejecutado: requestCode=" + requestCode + ", resultCode=" + resultCode);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
