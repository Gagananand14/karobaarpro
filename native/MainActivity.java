package com.gaganit.karobaarpro;

import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.getcapacitor.BridgeActivity;

/**
 * KarobaarPro: Capacitor activity + "AndroidPrint" bridge.
 * pos.html me window.AndroidPrint.print(html, widthMm, title) se bill system print dialog me khulta hai
 * (thermal receipt ke liye widthMm = 58 / 63 / 76 / 80 / 85, A4 ke liye 0).
 */
public class MainActivity extends BridgeActivity {

    private WebView printView; // print hone tak reference zinda rakhna zaroori hai

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBridge().getWebView().addJavascriptInterface(new PrintBridge(), "AndroidPrint");
    }

    private class PrintBridge {
        @JavascriptInterface
        public void print(final String html, final int widthMm, final String title) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final WebView w = new WebView(MainActivity.this);
                    printView = w;
                    w.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
                            if (pm == null) return;
                            PrintDocumentAdapter adapter = view.createPrintDocumentAdapter(title);
                            PrintAttributes.Builder b = new PrintAttributes.Builder();
                            if (widthMm > 0) {
                                int wMil = (int) (widthMm / 25.4 * 1000);   // 1/1000 inch
                                b.setMediaSize(new PrintAttributes.MediaSize("receipt", "Receipt " + widthMm + "mm", wMil, wMil * 4));
                            } else {
                                b.setMediaSize(PrintAttributes.MediaSize.ISO_A4);
                            }
                            b.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
                            pm.print(title, adapter, b.build());
                        }
                    });
                    w.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                }
            });
        }
    }
}
