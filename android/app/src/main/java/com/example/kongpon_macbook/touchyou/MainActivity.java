package com.example.kongpon_macbook.touchyou;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int PORT = 3000;
    static ProgressDialog pd;
    private ListView listView;
    final List<Host> availableHost = new ArrayList<>();
    ArrayAdapter<Host> adapter;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //  connectButton = (Button) findViewById(R.id.connectButton);
        //  ipEditText = (EditText) findViewById(R.id.ipEditText);

        /* Initialize views */
        setTitle("Select a Host Computer");
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                findServers();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        adapter = new ArrayAdapter<Host>(this, android.R.layout.simple_list_item_1, availableHost);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Host host = (Host) parent.getAdapter().getItem(position);
                if (!host.getAddress().equalsIgnoreCase("0")) {
                    System.out.println(host);
                    connect(host.getAddress());
                }
            }
        });
        pd = new ProgressDialog(this);
        findServers();

    }

    @Override
    protected void onResume() {
        super.onResume();
        findServers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search_item) {
            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
            final View mView = layoutInflaterAndroid.inflate(R.layout.user_input_dialog_box, null);
            AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
            alertDialogBuilderUserInput.setView(mView);
            final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);

            alertDialogBuilderUserInput
                    .setCancelable(false)
                    .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogBox, int id) {
                            String host = userInputDialogEditText.getText().toString();
                            connect(host);
                        }
                    })

                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogBox, int id) {
                                    dialogBox.cancel();
                                }
                            });

            AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
            alertDialogAndroid.show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void findServers() {
        mSwipeRefreshLayout.setRefreshing(true);
        new AsyncTask<String, Integer, Host[]>() {
            @Override
            protected Host[] doInBackground(String... params) {
                return ServerFinder.getAvailableHost();
            }

            @Override
            protected void onPostExecute(Host[] strings) {
                super.onPostExecute(strings);
                System.out.println(Arrays.toString(strings));
                System.out.println("On post execution");
                availableHost.clear();
                availableHost.addAll(Arrays.asList(strings));
                if (availableHost.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Could not find any host", Toast.LENGTH_LONG).show();
                    availableHost.add(new Host("Could not find any available servers","0"));
                }
                adapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);

            }
        }.execute();
    }


    private void connect(String host) {
        pd.setTitle("Connecting to " + host);
        pd.setMessage("Connecting...");
        new AsyncTask<String, Integer, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                TCPClient client = new TCPClient(params[0], PORT, MainActivity.this);
                try {
                    System.out.println("Start connecting");
                    client.openConnection();
                    Controller.getInstance().setConnection(client);
                } catch (IOException e) {
                    System.out.println("Connection Timeout");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                System.out.println("Done connecting");
                pd.dismiss();
            }
        }.execute(host);

        pd.show();
    }

    protected String wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e("WIFIIP", "Unable to get host address.");
            ipAddressString = null;
        }

        return ipAddressString;
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }
}
