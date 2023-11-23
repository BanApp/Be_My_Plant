package com.example.bemyplant

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.bemyplant.model.PlantModel
import com.example.bemyplant.module.PlantModule
import com.example.bemyplant.network.RetrofitService
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class SettingActivity : AppCompatActivity() {
    private val retrofitService = RetrofitService().apiService

    private lateinit var userImage: ImageView
    private lateinit var realNameTextView: TextView
    private lateinit var uidTextView: TextView
    //private val pushAlarmButton: TextView = findViewById(R.id.textView_sensor_temperature)
    private lateinit var logoutButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var deletePlantButton: Button
    private lateinit var modifySensorButton: Button
    private lateinit var defaultUserImage: Bitmap
    private lateinit var user_image: Bitmap
    lateinit var realm : Realm

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        userImage = findViewById(R.id.imageView_setting_user)
        realNameTextView = findViewById(R.id.textView_setting_name)
        uidTextView = findViewById(R.id.settingUid)
        logoutButton = findViewById(R.id.logoutButton)
        deleteAccountButton = findViewById(R.id.deleteAccountButton)
        deletePlantButton = findViewById(R.id.deletePlantButton)
        modifySensorButton = findViewById(R.id.modifySensorButton)
        // 계정 정보 API 호출 -> 계정, 실제 이름대로 uidTextView, nameTextView 수정
        getUserAccount()

        // TODO: 사용자 이미지 변경할 것
        userImage.setImageResource(R.drawable.user_image)

        // TODO: 2. (정현) 식물 DB에서 식물 이름 가져옴 -> nameTextView 수정

        // (1) 로그아웃 버튼 클릭 시 처리
        logoutButton.setOnClickListener{
            logoutPopup() //팝업창 띄움
        }

        // (2) 회원탈퇴 버튼 클릭 시 처리
        deleteAccountButton.setOnClickListener{
            deleteAccountPopup() //팝업창 띄움
        }

        // (3) 식물삭제 버튼 클릭 시 처리
        deletePlantButton.setOnClickListener{
            deletePlantPopup() //팝업창 띄움

        }
        //(4) 센서 변경
        modifySensorButton.setOnClickListener{
            val subNav1 = Intent(this@SettingActivity, TempConnectActivity::class.java)
            startActivity(subNav1)
        }

        // 하단바
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation_main_menu)

        bottomNavigationView.selectedItemId = R.id.menu_setting
        val menuView = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
//        val selectedItemIndex = 3
//
//        menuView.getChildAt(selectedItemIndex).performClick()

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    // "홈" 메뉴 클릭 시 MainActivity로 이동
                    val homeIntent = Intent(this@SettingActivity, MainActivity::class.java)
                    startActivity(homeIntent)
                    true
                }

                R.id.menu_setting -> {
                    // "setting" 메뉴 클릭 시 SettingActivity로 이동
                    val boardIntent = Intent(this@SettingActivity, SettingActivity::class.java)
                    startActivity(boardIntent)
                    true
                }

                R.id.menu_chat -> {
                    // "채팅" 메뉴 클릭 시 ChatActivity로 이동
                    val chatIntent = Intent(this@SettingActivity, ChatActivity::class.java)
                    startActivity(chatIntent)
                    true
                }

                R.id.menu_diary -> {
                    // "일기" 메뉴 클릭 시 DiaryActivity로 이동
                    val diaryIntent = Intent(this@SettingActivity, DiaryActivity::class.java)
                    startActivity(diaryIntent)
                    true
                }

                else -> false
            }
        }
    }

    private fun getUserAccount() {
        val sharedPreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        val Tag: String = "sensor"
        //Log.d(Tag, "token: $token")
        val mainIntent = getIntent()
        val P_Name = mainIntent.getStringExtra("P_Name").toString()

        // 토큰 부재 시 초기화면(MJ_main)으로 전환
        if (token.isNullOrEmpty()){
            val homeIntent = Intent(this@SettingActivity, MJ_MainActivity::class.java)
            startActivity(homeIntent)
        }
        CoroutineScope(Dispatchers.IO).launch {
            val response = retrofitService.getUserData("Bearer " + token)
            val Tag: String = "sensor"
            Log.d(Tag, "sensor raw-response: $response")

            if (response.isSuccessful) {
                val UserData = response.body()

                runOnUiThread {
                    realNameTextView.text = response.body()?.r_name + "(${P_Name} 주인님)"
                    uidTextView.text = response.body()?.username
                    // 화면 갱신
                    realNameTextView.invalidate()
                    uidTextView.invalidate()
                }
            }
        }
    }

    fun logoutPopup() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.fragment_setting_logout_popup, null)

        // 팝업 창의 뷰로 사용할 XML 레이아웃 설정
        builder.setView(dialogView)

        val dialog = builder.create()

        // 예 버튼 클릭 시의 동작
        dialogView.findViewById<AppCompatButton>(R.id.appCompatButton_logout_yes).setOnClickListener {
            // 로그아웃 (sharedProferences 삭제)
            val sharedPreferences = this.getSharedPreferences("Prefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences?.edit()
            // "token" 데이터 삭제
            editor?.remove("token")

            // 변경 사항 적용
            editor?.apply()

            // 최초 화면으로 이동 (MJ_main)
            val homeIntent = Intent(this@SettingActivity, MJ_MainActivity::class.java)
            startActivity(homeIntent)
            dialog.dismiss()
        }

        // 아니오 버튼 클릭 시의 동작
        dialogView.findViewById<AppCompatButton>(R.id.appCompatButton_logout_no).setOnClickListener {
            dialog.dismiss() // 다이얼로그를 닫습니다.
        }

        dialog.show()

    }

    fun deleteAccountPopup(){
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.fragment_setting_withdrawal_popup, null)

        // 팝업 창의 뷰로 사용할 XML 레이아웃 설정
        builder.setView(dialogView)

        val dialog = builder.create()

        // 예 버튼 클릭 시의 동작
        dialogView.findViewById<AppCompatButton>(R.id.appCompatButton_withdrawal_yes).setOnClickListener {
            // 2. 회원 탈퇴 (rest API 로 계정 delete)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val sharedPreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE)
                    val token = sharedPreferences.getString("token", null)

                    // 토큰 부재 시 초기화면(MJ_main)으로 전환
                    if (token.isNullOrEmpty()){
                        val homeIntent = Intent(this@SettingActivity, MJ_MainActivity::class.java)
                        startActivity(homeIntent)
                    }

                    // API 요청 보내기
                    val response = retrofitService.deleteAccount("Bearer " + token)
                    Log.d("setting", "token: ${token}")
                    Log.d("setting", "response: $response")
                    if (response.isSuccessful) {
                        // 회원 탈퇴 성공
                        withContext(Dispatchers.Main) {
                            showToast(this@SettingActivity, "회원탈퇴 되었습니다.")
                        }
                        // 최초 화면으로 이동 (MJ_main)
                        val homeIntent = Intent(this@SettingActivity, MJ_MainActivity::class.java)
                        startActivity(homeIntent)
                    } else {
                        // 회원탈퇴 실패
                        withContext(Dispatchers.Main) {
                            showToast(this@SettingActivity, "회원탈퇴 실패")
                        }
                    }
                } catch (e: Exception) {
                    // API 요청 실패
                    withContext(Dispatchers.Main) {
                        showToast(this@SettingActivity, "API 요청 실패: ${e.message}")
                    }
                }
            }
        }

        // 아니오 버튼 클릭 시의 동작
        dialogView.findViewById<AppCompatButton>(R.id.appCompatButton_withdrawal_no).setOnClickListener {
            dialog.dismiss() // 다이얼로그를 닫습니다.
        }

        dialog.show()

    }

    fun deletePlantPopup(){
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.fragment_setting_delete_plant_popup, null)




        // 팝업 창의 뷰로 사용할 XML 레이아웃 설정
        builder.setView(dialogView)

        val dialog = builder.create()

        // 예 버튼 클릭 시의 동작
        dialogView.findViewById<AppCompatButton>(R.id.appCompatButton_deletePlant_yes).setOnClickListener {
            // 2. 식물 삭제
            // TODO: 3. (정현) 식물 DB에서 식물 삭제
            // sdhan :realm DB control : DB 초기화 or 지우기
            Realm.init(this)
            val configPlant : RealmConfiguration = RealmConfiguration.Builder()
                .name("appdb.realm") // 생성할 realm 파일 이름 지정
                .deleteRealmIfMigrationNeeded()
                .modules(PlantModule())
                .allowWritesOnUiThread(true) // sdhan : UI thread에서 realm에 접근할수 있게 허용
                .build()
            realm = Realm.getInstance(configPlant)

            realm.executeTransaction {
                //전부지우기
                it.where(PlantModel::class.java).findAll().deleteAllFromRealm()
                //첫번째 줄 지우기
    //            it.where(PlantModel::class.java).findFirst()?.deleteFromRealm()
            }
            val dateFormat = "yyyy-MM-dd"
            val date = Date(System.currentTimeMillis())
            val simpleDateFormat = SimpleDateFormat(dateFormat)
            val simpleDate: String = simpleDateFormat.format(date)
            Log.i("iiiii : ", simpleDate)
            realm.executeTransaction{
                with(it.createObject(PlantModel::class.java)){
                    this.P_Name = ""
                    this.P_Birth = simpleDate
                    this.P_Race = ""
//                this.P_Image =
                    this.P_Registration = ""
                }
            }

            // 비동기로 처리 ? (고려중)

            // 식물 이미지 변경 (+)
            val deletePlant = R.drawable.delete_plant
            val bundle = Bundle()
            bundle.putInt("newPlantImageResId", deletePlant)

            // 화면 이동 (메인화면 이동)
            val mainActivityIntent = Intent(this@SettingActivity, MainActivity::class.java)
            mainActivityIntent.putExtras(bundle)
            startActivity(mainActivityIntent)
        }

        // 아니오 버튼 클릭 시의 동작
        dialogView.findViewById<AppCompatButton>(R.id.appCompatButton_deletePlant_no).setOnClickListener {
            dialog.dismiss() // 다이얼로그를 닫습니다.
        }

        dialog.show()
    }

    fun readBitmapFromExternalStorage(file: File): Bitmap? {
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun drawableResourceToBitmap(context: Context, drawableResId: Int): Bitmap? {
        return runBlocking {
            withContext(Dispatchers.IO) {
                // Drawable을 가져옵니다.
                val drawable = ContextCompat.getDrawable(context, drawableResId)

                // Drawable을 Bitmap으로 변환합니다.
                drawableToBitmap(drawable)
            }
        }
    }

    // 변환 함수
    fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

}