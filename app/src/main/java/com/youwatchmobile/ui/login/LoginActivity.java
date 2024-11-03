package com.youwatchmobile.ui.login;

import android.app.Activity;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.youwatchmobile.R;
import com.youwatchmobile.api.ApiService;
import com.youwatchmobile.api.RetrofitClient;
import com.youwatchmobile.databinding.ActivityLoginBinding;
import com.youwatchmobile.model.LoginRequest;
import com.youwatchmobile.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ApiService apiService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.youwatchmobile.databinding.ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar o Retrofit
        apiService = RetrofitClient.getInstance();

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Habilitar o botão de login se os campos estiverem preenchidos
                loginButton.setEnabled(!usernameEditText.getText().toString().isEmpty() &&
                        !passwordEditText.getText().toString().isEmpty());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin(usernameEditText.getText().toString(), passwordEditText.getText().toString(), loadingProgressBar);
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                attemptLogin(usernameEditText.getText().toString(), passwordEditText.getText().toString(), loadingProgressBar);
            }
        });
    }

    private void attemptLogin(String username, String password, ProgressBar loadingProgressBar) {
        // Criar um objeto LoginRequest com o e-mail e a senha
        LoginRequest loginRequest = new LoginRequest(username, password);

        // Fazer a requisição de login usando Retrofit
        Call<LoginResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                loadingProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    // Salva o token recebido
                    saveToken(response.body().getToken());

                    // Atualiza a interface com a resposta do login
                    updateUiWithUser(username);
                    setResult(Activity.RESULT_OK);

                    // Fecha a atividade ao fazer login com sucesso
                    finish();
                } else {
                    showLoginFailed(R.string.login_failed);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loadingProgressBar.setVisibility(View.GONE);
                Log.e("LoginError", "Falha na requisição: " + t.getMessage());
                showLoginFailed(R.string.login_failed);
            }
        });
    }

    // Método para salvar o token em SharedPreferences
    private void saveToken(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences("YouWatchPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("auth_token", token);
        editor.apply();
    }




    private void updateUiWithUser(String email) {
        String welcome = getString(R.string.welcome) + " " + email;
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
