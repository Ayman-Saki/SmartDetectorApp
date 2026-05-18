package org.tensorflow.lite.examples.imageclassification.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.tensorflow.lite.examples.imageclassification.R;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

public class QrFragment extends Fragment {

    private ImageView qrImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_qr_display, container, false);

        qrImage = view.findViewById(R.id.qrImage);


        String data = "https://www.best-selling-cars.com/";

        qrImage.setImageBitmap(generateQR(data));

        return view;
    }

    private Bitmap generateQR(String value) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(value, BarcodeFormat.QR_CODE, 500, 500);

            Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.RGB_565);

            for (int x = 0; x < 500; x++) {
                for (int y = 0; y < 500; y++) {
                    bitmap.setPixel(x, y,
                            bitMatrix.get(x, y) ?
                                    android.graphics.Color.BLACK :
                                    android.graphics.Color.WHITE);
                }
            }

            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}