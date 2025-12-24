package com.zds.ydkf_qm;


import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus, tvResult;
    private Uri pickedUri = null;
    private String cardSide = "FRONT";

    private final ActivityResultLauncher<String> pickLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                pickedUri = uri;
                if (uri != null) {
                    tvStatus.setText("状态：已选择图片 -> " + uri);
                } else {
                    tvStatus.setText("状态：未选择图片");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPick = findViewById(R.id.btnPick);
        Button btnFront = findViewById(R.id.btnFront);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnOcr = findViewById(R.id.btnOcr);

        tvStatus = findViewById(R.id.tvStatus);
        tvResult = findViewById(R.id.tvResult);

        btnPick.setOnClickListener(v -> pickLauncher.launch("image/*"));

        btnFront.setOnClickListener(v -> {
            cardSide = "FRONT";
            tvStatus.setText("状态：已选择识别面 -> FRONT（头像面）");
        });

        btnBack.setOnClickListener(v -> {
            cardSide = "BACK";
            tvStatus.setText("状态：已选择识别面 -> BACK（国徽面）");
        });

        btnOcr.setOnClickListener(v -> {
            if (pickedUri == null) {
                tvStatus.setText("状态：请先选择图片");
                return;
            }
            tvStatus.setText("状态：图片转Base64中...");
            tvResult.setText("");

            String base64;
            try {
                base64 = Base64Util.uriToBase64(this, pickedUri, 1280, 85);
            } catch (Exception e) {
                tvStatus.setText("状态：Base64失败");
                tvResult.setText(String.valueOf(e));
                return;
            }

            tvStatus.setText("状态：请求腾讯云OCR中...");

            TencentHttp.idCardOcr(base64, "FRONT", new TencentHttp.Callback() {
                @Override
                public void onSuccess(String json) {
                    runOnUiThread(() -> {
                        tvStatus.setText("状态：成功 ✅");
                        tvResult.setText(json);
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        tvStatus.setText("状态：失败");
                        tvResult.setText(error);
                    });
                }
            });
        });
    }
}
