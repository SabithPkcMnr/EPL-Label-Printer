package com.sabithpkcmnr.elpprinter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.caysn.autoreplyprint.AutoReplyPrint;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import java.lang.reflect.Method;

public class ActivityPrinter extends AppCompatActivity {

    int nPageID = 1;

    ComboBox cbxListBT2;
    ActivityPrinter activity;
    ListView listViewTestFunction;
    Button btnOpenPort, btnClosePort;
    Pointer myPointer = Pointer.NULL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);
        setTitle(getResources().getString(R.string.app_name) + " " + AutoReplyPrint.INSTANCE.CP_Library_Version());

        setupLayoutViews();

        setupBluetoothList();

        setupPrinterCallback();

        setupBluetoothPrinter();
    }

    private void setupLayoutViews() {
        activity = this;

        cbxListBT2 = findViewById(R.id.cbxLisbBT2);
        btnOpenPort = findViewById(R.id.btnOpenPort);
        btnClosePort = findViewById(R.id.btnClosePort);

        Button btnPrint = findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                printImageNow(myPointer);
            }
        });

        listViewTestFunction = findViewById(R.id.listViewTestFunction);

        btnOpenPort.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenPort();
            }
        });

        btnClosePort.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClosePort();
            }
        });
    }

    private void setupBluetoothList() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, PrinterFunction.testFunctionOrderedList);
        listViewTestFunction.setAdapter(adapter);
        listViewTestFunction.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String functionName = ((TextView) view).getText().toString();
                if (functionName.isEmpty())
                    return;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            PrinterFunction fun = new PrinterFunction();
                            fun.ctx = activity;
                            Method m = PrinterFunction.class.getDeclaredMethod(functionName, Pointer.class);
                            m.invoke(fun, myPointer);
                        } catch (Throwable tr) {
                            tr.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    private void setupPrinterCallback() {
        AutoReplyPrint.INSTANCE.CP_Port_AddOnPortOpenedEvent(new AutoReplyPrint.CP_OnPortOpenedEvent_Callback() {
            @Override
            public void CP_OnPortOpenedEvent(Pointer pointer, String s, Pointer pointer1) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Open Success", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, Pointer.NULL);
        AutoReplyPrint.INSTANCE.CP_Port_AddOnPortOpenFailedEvent(new AutoReplyPrint.CP_OnPortOpenFailedEvent_Callback() {
            @Override
            public void CP_OnPortOpenFailedEvent(Pointer pointer, String s, Pointer pointer1) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Open Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, Pointer.NULL);
        AutoReplyPrint.INSTANCE.CP_Port_AddOnPortClosedEvent(new AutoReplyPrint.CP_OnPortClosedEvent_Callback() {
            @Override
            public void CP_OnPortClosedEvent(Pointer pointer, Pointer pointer1) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ClosePort();
                    }
                });
            }
        }, Pointer.NULL);
    }

    private void setupBluetoothPrinter() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IntByReference cancel = new IntByReference(0);
                AutoReplyPrint.CP_OnBluetoothDeviceDiscovered_Callback callback = new AutoReplyPrint.CP_OnBluetoothDeviceDiscovered_Callback() {
                    @Override
                    public void CP_OnBluetoothDeviceDiscovered(String device_name, final String device_address, Pointer private_data) {

                        Log.d("logHomeDataY","Name: " + device_name + " - ID: " + device_address);

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!cbxListBT2.getData().contains(device_address))
                                    cbxListBT2.addString(device_address);
                                if (cbxListBT2.getText().trim().equals("")) {
                                    cbxListBT2.setText(device_address);
                                }
                            }
                        });
                    }
                };
                AutoReplyPrint.INSTANCE.CP_Port_EnumBtDevice(12000, cancel, callback, null);
            }
        }).start();
    }

    private void OpenPort() {
        final String strBT2Address = cbxListBT2.getText();
        new Thread(new Runnable() {
            @Override
            public void run() {
                myPointer = AutoReplyPrint.INSTANCE.CP_Port_OpenBtSpp(strBT2Address, 0);
            }
        }).start();
    }

    private void ClosePort() {
        if (myPointer != Pointer.NULL) {
            AutoReplyPrint.INSTANCE.CP_Port_Close(myPointer);
            myPointer = Pointer.NULL;
        }
    }

    private void printImageNow(Pointer pointer) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.my_sticker);

        if ((bitmap == null) || (bitmap.getWidth() == 0) || (bitmap.getHeight() == 0))
            return;

        int dstw = 380;
        int dsth = (int) (dstw * ((double) bitmap.getHeight() / bitmap.getWidth()));

        int topMargin = 12;
        AutoReplyPrint.INSTANCE.CP_Label_PageBegin(pointer, 0, 0, dstw, dsth + topMargin, AutoReplyPrint.CP_Label_Rotation_0);
        AutoReplyPrint.CP_Label_DrawImageFromData_Helper.DrawImageFromBitmap(pointer, 0, topMargin, dstw, dsth, bitmap, AutoReplyPrint.CP_ImageBinarizationMethod_Thresholding);
        AutoReplyPrint.INSTANCE.CP_Label_PagePrint(pointer, 1);

        boolean result = AutoReplyPrint.INSTANCE.CP_Pos_QueryPrintResult(pointer, nPageID++, 30000);
        if (!result)
            PrinterHelper.showMessageOnUiThread(this, "Print failed");
        else
            PrinterHelper.showMessageOnUiThread(this, "Print Success");
    }

    @Override
    protected void onDestroy() {
        RemoveCallback();
        ClosePort();
        super.onDestroy();
    }

    private void RemoveCallback() {
        AutoReplyPrint.INSTANCE.CP_Port_RemoveOnPortOpenedEvent(new AutoReplyPrint.CP_OnPortOpenedEvent_Callback() {
            @Override
            public void CP_OnPortOpenedEvent(Pointer pointer, String s, Pointer pointer1) {
            }
        });
        AutoReplyPrint.INSTANCE.CP_Port_RemoveOnPortOpenFailedEvent(new AutoReplyPrint.CP_OnPortOpenFailedEvent_Callback() {
            @Override
            public void CP_OnPortOpenFailedEvent(Pointer pointer, String s, Pointer pointer1) {

            }
        });
        AutoReplyPrint.INSTANCE.CP_Port_RemoveOnPortClosedEvent(new AutoReplyPrint.CP_OnPortClosedEvent_Callback() {
            @Override
            public void CP_OnPortClosedEvent(Pointer pointer, Pointer pointer1) {

            }
        });
    }

}