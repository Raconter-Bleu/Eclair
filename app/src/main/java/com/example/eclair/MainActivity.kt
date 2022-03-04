package com.example.eclair

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.allenliu.classicbt.BluetoothPermissionHandler
import com.allenliu.classicbt.CLog
import com.allenliu.classicbt.Connect
import com.allenliu.classicbt.listener.PacketDefineListener
import com.allenliu.classicbt.listener.TransferProgressListener
import com.example.eclair.databinding.ActivityMainBinding
import android.os.Handler
import android.widget.ListView
import android.widget.TextView
import com.allenliu.classicbt.BleManager
import com.allenliu.classicbt.scan.ScanConfig
import com.allenliu.classicbt.listener.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.app.ProgressDialog
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(),BluetoothPermissionCallBack
{
    var MyBTContext: Context? = null
    var listView: ListView? = null
    var listViewAdapter: Adapter? = null
    var connectDialog: ProgressDialog? = null
    val ecServerId = "0000FFF0-0000-1000-8000-00805F9B34FB"
    val ecWriteCharacteristicId = "0000FFF2-0000-1000-8000-00805F9B34FB"
    val ecReadCharacteristicId = "0000FFF1-0000-1000-8000-00805F9B34FB"

    var deviceListData: MutableList<DeviceInfo> = ArrayList()
    var connectionStateChangeCallback: (ok: Boolean) -> Unit = { _ -> }

    var bluetoothGatt: BluetoothGatt? = null
    var getServicesCallback: (servicesList: List<String>) -> Unit = { _ -> }
    var characteristicChangedCallback: (hex: String, string: String) -> Unit = { _, _ -> }

    private lateinit var list: ArrayList<BluetoothDevice>


    var connectCallback: (ok: Boolean, errCode: Int) -> Unit = { _, _ -> }


    var connect: Connect? = null
    //包 的开头结尾定义
    val start = "".toByteArray()
    val end = "".toByteArray()

    private val permissionCallBack = BluetoothPermissionHandler(this, this)

    //写设备列表
    @SuppressLint("MissingPermission")
    private fun isContained(result: BluetoothDevice): Boolean {
        if (result.name == null || "null".equals(result.name, ignoreCase = true))
            return true
        for (device in list) {
            if (result.address == device.address) {
                return true
            }
        }
        return false
    }

    //三个前置函数
    override fun permissionFailed() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        permissionCallBack.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionCallBack.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
//三个前置函数
    var ran1=Random(47)
final val rssi1= (ran1.nextInt(26)+41)

    override fun onBlueToothEnabled()
    {
BleManager.getInstance().init(application)
        BleManager.getInstance().setForegroundService(true)
        BleManager.getInstance().scan(ScanConfig(4000), object : ScanResultListener
        {
            @SuppressLint("MissingPermission")
            override fun onDeviceFound(device: BluetoothDevice?)
            {

                if (!isContained(device!!))
                {

                    list.add(device)

                    
                    deviceListData.add(DeviceInfo(device.name,rssi1))

                        //recyclerview.adapter?.notifyDataSetChanged()
                }
            }


            override fun onError()
            {
                TODO("Not yet implemented")
            }

            override fun onFinish()
            {
                TODO("Not yet implemented")
            }

        })
    }



    //连接状态
    fun registerServer() {
        BleManager.getInstance().registerServerConnection(object : ConnectResultlistner {
            override fun disconnected() {
                t("bluetooth has disconnected")
                BleManager.getInstance().destory()
                registerServer()
            }

            override fun connectSuccess(connect: Connect?) {
                this@MainActivity.connect = connect
                read()
            }

            override fun connectFailed(e: Exception?) {

            }

        })
    }





    //善后
    override fun onDestroy() {
        super.onDestroy()
        BleManager.getInstance().destory()
    }

//上面是BT
    //
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
setContentView(R.layout.fragment_home)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

///BT的功能
        list = ArrayList()
        permissionCallBack.start()

    }

    fun read() {
//        val a:Int= -0x146f1470
//        val buffer=ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(a)
//        val b:Byte= 0x03
//        val buffer2=ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN).put(b)
        connect?.setReadPacketVerifyListener(object : PacketDefineListener {
            override fun getPacketStart(): ByteArray {
                return start

            }

            override fun getPacketEnd(): ByteArray {
                return end
            }
        })

        connect?.read(object : TransferProgressListener {

            override fun transferSuccess(bytes: ByteArray?) {
                t("received message")


                bytes?.let { it1 ->
                   /* tvReceive.text = String(it1)*/

                }

                CLog.e("read string")
            }

            override fun transferFailed(msg: Exception) {
                msg.printStackTrace()
//                msg.message?.run {
//                    t(this)
//                }

            }

            override fun transfering(progress: Int) {
                CLog.e("read progress:$progress")
            }
        })
    }


    class DeviceInfo(var name: String, var rssi: Int)


    class Adapter(context: Context, val resourceId: Int, objects: List<DeviceInfo>) :
        ArrayAdapter<DeviceInfo>(context, resourceId, objects) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val deviceInfo: DeviceInfo? = getItem(position) //获取当前项的实例
            val name = deviceInfo?.name ?: ""
            val rssi = deviceInfo?.rssi ?: 0
            val view: View = LayoutInflater.from(context).inflate(resourceId, parent, false)
            val headImg = view.findViewById<ImageView>(R.id.iv_type)
                headImg.setImageResource(R.drawable.ble)

            view.findViewById<TextView>(R.id.tv_name).text = name
            view.findViewById<TextView>(R.id.tv_rssi).text = "" + rssi
            val rssiImg = view.findViewById<ImageView>(R.id.iv_rssi)
            when {
                rssi >= -41 -> rssiImg.setImageResource(R.drawable.s5)
                rssi >= -55 -> rssiImg.setImageResource(R.drawable.s4)
                rssi >= -65 -> rssiImg.setImageResource(R.drawable.s3)
                rssi >= -75 -> rssiImg.setImageResource(R.drawable.s2)
                rssi < -75 -> rssiImg.setImageResource(R.drawable.s1)
            }

            return view
        }
    }

    fun showToast(text: String) {
        runOnUiThread {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
    }

fun uiInit()
{
    //下拉刷新
    val swipRefreshLayout: SwipeRefreshLayout = findViewById(R.id.swipe_layout)
    swipRefreshLayout.setColorSchemeColors(0x01a4ef)
    swipRefreshLayout.setOnRefreshListener {
        //清空数据
        list.clear()
        listViewAdapter?.notifyDataSetChanged()
        Handler().postDelayed({
            swipRefreshLayout.isRefreshing = false
            onBlueToothEnabled()
        }, 1000)
    }

    //列表初始化
    listView=findViewById(R.id.list_view)
    listViewAdapter= Adapter(this,R.layout.list_item,deviceListData)
    listView?.adapter=listViewAdapter
    listView?.setOnItemClickListener{ adapterView:AdapterView<*>, view1:View, i:Int, l:Long ->
        showConnectDialog()
        myConnect(deviceListData.get(i).name) {
            hideConnectDialog()
            if (it)
            {
                showToast("Connect Success")

            }

        }
    }


}

    fun hideConnectDialog() {
        runOnUiThread {
            connectDialog?.dismiss()
        }
    }
    fun showConnectDialog() {
        runOnUiThread {
            if (connectDialog == null) {
                connectDialog = ProgressDialog(this)
                connectDialog?.setMessage("Connecting...")
            }
            connectDialog?.show()
        }
    }


    var reconnectTime = 0
    fun myConnect(name: String, callback: (ok: Boolean) -> Unit) {
        myOneConnect(name) {
            if (it) {
                reconnectTime = 0
                callback(true)
            } else {
                reconnectTime = reconnectTime + 1
                if(reconnectTime>4){
                    reconnectTime = 0
                    callback(false)
                }
                else{
                    thread(start = true) {
                        myConnect(name,callback)
                    }
                }
            }
        }
    }

    fun myOneConnect(name: String, callback: (ok: Boolean) -> Unit) {
        createMyConnection(name) { ok: Boolean, errCode: Int ->
//            Log.e("Connection", "res:" + ok + "|" + errCode)
            if (ok) {
//                onBLECharacteristicValueChange { hex: String, string: String ->
//                    Log.e("hex", hex)
//                    Log.e("string", string)
//                }
                getMyDeviceServices() {
//                    for (item in it) {
//                        Log.e("ble-service", "UUID=" + item)
//                    }
                    //"00001101-0000-1000-8000-00805F9B34FB" 本框架总id
                    //
                    //EC框架id：
                    getMyDeviceCharacteristics(ecServerId)
                    notifyMyCharacteristicValueChange(ecServerId, ecReadCharacteristicId)
                    callback(true)
                    Thread() {
                        Thread.sleep(300);
                        setMtu(500)
                    }.start()
                }
            } else {
                callback(false)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun createMyConnection(name: String, callback: (ok: Boolean, errCode: Int) -> Unit) {
        connectCallback = callback
        connectionStateChangeCallback = { _ -> }
        var isExist: Boolean = false
        for (item in list) {
            if (item.name == name) {
                bluetoothGatt =item.connectGatt(MyBTContext,false,bluetoothGattCallback)

//item.bluetoothDevice.connectGatt(bleContext, false, bluetoothGattCallback)
//isExist = true
                break;
            }
        }
        if (!isExist) {
            connectCallback(false, -1)
        }
    }

    var bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback()
    {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
//            Log.e("onConnectionStateChange", "status=" + status + "|" + "newState=" + newState);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                connectCallback(false, status)
                connectCallback = { _, _ -> }
                connectionStateChangeCallback(false)
                connectionStateChangeCallback = { _ -> }
                return
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectCallback(true, 0)
                connectCallback = { _, _ -> }
                return
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                bluetoothGatt?.close()
                connectCallback(false, 0)
                connectCallback = { _, _ -> }
                connectionStateChangeCallback(false)
                connectionStateChangeCallback = { _ -> }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            bluetoothGatt = gatt
            val bluetoothGattServices = gatt?.services
            val servicesList: MutableList<String> = ArrayList()
            if (bluetoothGattServices == null) getServicesCallback(servicesList)
            else {
                for (item in bluetoothGattServices) {
//                    Log.e("ble-service", "UUID=:" + item.uuid.toString())
                    servicesList.add(item.uuid.toString())
                }
                getServicesCallback(servicesList)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            val bytes = characteristic?.value
            if (bytes != null) {
//                Log.e("ble-receive", "读取成功[hex]:" + bytesToHexString(bytes));
//                Log.e("ble-receive", "读取成功[string]:" + String(bytes));
                characteristicChangedCallback(bytesToHexString(bytes), String(bytes))
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
//            if (BluetoothGatt.GATT_SUCCESS == status) {
//                Log.e("BleService", "onMtuChanged success MTU = " + mtu)
//            } else {
//                Log.e("BleService", "onMtuChanged fail ");
//            }
        }
    }

    fun bytesToHexString(bytes: ByteArray?): String {
        if (bytes == null) return ""
        var str = ""
        for (b in bytes) {
            str += String.format("%02X", b)
        }
        return str
    }

    @SuppressLint("MissingPermission")
    private fun getMyDeviceServices(callback: (servicesList: List<String>) -> Unit) {
        getServicesCallback = callback
        bluetoothGatt?.discoverServices();
    }

    private fun getMyDeviceCharacteristics(serviceId: String): MutableList<String> {
        val service = bluetoothGatt?.getService(UUID.fromString(serviceId))
        val listGattCharacteristic = service?.getCharacteristics()
        val characteristicsList: MutableList<String> = ArrayList()
        if (listGattCharacteristic == null) return characteristicsList
        for (item in listGattCharacteristic) {
//            Log.e("ble-characteristic", "UUID=:" + item.uuid.toString())
            characteristicsList.add(item.uuid.toString())
        }
        return characteristicsList
    }

    @SuppressLint("MissingPermission")
    private fun notifyMyCharacteristicValueChange(
        serviceId: String,
        characteristicId: String
    ): Boolean {
        val service = bluetoothGatt?.getService(UUID.fromString(serviceId)) ?: return false
        val characteristicRead = service.getCharacteristic(UUID.fromString(characteristicId));
        val res =
            bluetoothGatt?.setCharacteristicNotification(characteristicRead, true) ?: return false
        if (!res) return false
        for (dp in characteristicRead.descriptors) {
            dp.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            bluetoothGatt?.writeDescriptor(dp)
        }
        return true
    }

    @SuppressLint("MissingPermission")
    fun setMtu(v: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothGatt?.requestMtu(v)
        }
    }











}







