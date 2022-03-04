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
import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.widget.ListView
import android.widget.TextView
import com.allenliu.classicbt.BleManager
import com.allenliu.classicbt.scan.ScanConfig
import com.allenliu.classicbt.listener.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.Manifest
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import java.util.Random



class MainActivity : AppCompatActivity(),BluetoothPermissionCallBack
{
    var listView: ListView? = null
    var listViewAdapter: Adapter? = null

    var deviceListData: MutableList<DeviceInfo> = ArrayList()

    private lateinit var list: ArrayList<BluetoothDevice>



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
                    tvReceive.text = String(it1)

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
    listViewAdapter=Adapte


}





}





