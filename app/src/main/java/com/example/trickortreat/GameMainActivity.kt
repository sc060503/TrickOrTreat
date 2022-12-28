package com.example.trickortreat

import android.content.Intent
import android.hardware.display.DisplayManager
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.Session
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.util.*
import kotlin.concurrent.timer


class GameMainActivity : AppCompatActivity() {
	var mSession: Session? = null
	var myGLRenderer: MyGLRenderer? = null
	var myGLView: GLSurfaceView? = null

	var isFirstObjCreate = true
	var frame: Frame? = null
	var needCreateObjNum = 0

	var senterPose :Pose? = null // 중앙 포즈
	var senterPoseX = 0f // 화면 중앙좌표 XYZ (최초 획득 후 고정)
	var senterPoseY = 0f
	var senterPoseZ = 0f

	var senterMatrix:FloatArray? = null // 게임화면 중앙좌표
	var modelMatrix:FloatArray? = null // 오브젝트 생성좌표

	var isRankingMode = false	// 모드 선택값을 받아오기 위한 Boolean 변수
	var level = ""				// 난이도 선택값을 받아오기 위한 String 변수

	var gametimer: ProgressBar? = null
	var scoreTxt: TextView? = null

	var btnPause: Button? = null

	var txtMyNum: TextView? = null
	var num = 0

	var btnAnswer: Button? = null

	var victory = false
	var nickname: String? = null

	var plus1: Button? = null
	var plus10: Button? = null
	var minus1: Button? = null
	var minus10: Button? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
		setContentView(R.layout.activity_game_main)

		myGLView = findViewById(R.id.myGLView)

		gametimer = findViewById(R.id.timer)
		gametimer!!.max = 20000
		gametimer!!.setProgress(20000)
//		scoreTxt = findViewById(R.id.scoreTxt)

		btnPause = findViewById(R.id.btnPause)
		txtMyNum = findViewById(R.id.txtMyNum)

		plus1 = findViewById(R.id.plus1)
		plus10 = findViewById(R.id.plus10)
		minus1 = findViewById(R.id.minus1)
		minus10 = findViewById(R.id.minus10)

		val sharedPreference = getSharedPreferences("memInfo", 0)
		nickname = sharedPreference.getString("nickname", "")

		btnPause!!.setOnClickListener {
			myGLRenderer!!.pause = true
			timer?.cancel()
			val dialog = PauseFragment()
			dialog.show(supportFragmentManager, "PauseFragment")
		}

		btnAnswer = findViewById(R.id.btnAnswer)

		btnAnswer!!.setOnClickListener {
			if (needCreateObjNum == num && !isFirstObjCreate) {
				Toast.makeText(this,"정답입니다.",Toast.LENGTH_SHORT).show()
				victory = true
				timer?.cancel()
				ms = 0
				runOnUiThread {
					gametimer!!.setProgress(20000)
				}
				record = score
				score = 0
				isrun = false
				myGLRenderer!!.pause = true
				val dialog = ResultFragment()
				dialog.show(supportFragmentManager, "ResultFragment")

			} else if (needCreateObjNum != num && !isFirstObjCreate) {
				Toast.makeText(this,"오답입니다.",Toast.LENGTH_SHORT).show()
			} else if (isFirstObjCreate) {
				Toast.makeText(this,"게임은 정정당당하게",Toast.LENGTH_SHORT).show()
			}
		}

		// 로비 화면에서 Intent로 보낸 모드 선택값을 받아온다
		val intent = getIntent()
		isRankingMode = intent.getBooleanExtra("mode", false)
		if (!isRankingMode) {
			level = intent.getStringExtra("level").toString()
		}
		// 난이도 값 : 쉬움(Easy), 보통(Normal), 어려움(Hard)

		when(level) {
			"Easy" ->    needCreateObjNum = (4..10).random()
			"Normal" -> needCreateObjNum = (10..17).random() // 10~17
			"Hard" ->    needCreateObjNum = (17..25).random() // 17~25
			"" ->       needCreateObjNum = (20..40).random() // 20~40
		}

		myGLView!!.setEGLContextClientVersion(3)
		myGLView!!.preserveEGLContextOnPause=true //일시중지시 EGLContext 유지여부
		myGLRenderer = MyGLRenderer(this, needCreateObjNum) //어떻게 그릴 것인가
		myGLView!!.setRenderer(myGLRenderer)

		//화면 렌더링을 언제 할 것인가 = 렌더러 반복호출하여 장면을 다시 그린다.
		myGLView!!.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

		//화면 변화 감지
		val displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager

		displayManager.registerDisplayListener(
			object: DisplayManager.DisplayListener{
				override fun onDisplayAdded(displayId: Int) {}
				override fun onDisplayRemoved(displayId: Int) {}
				override fun onDisplayChanged(displayId: Int) {
					synchronized(this){
						//화면 변경 와
						myGLRenderer!!.viewportChange = true
					}
				}
			}, null
		)
	}

	override fun onResume() { // 딴화면에서 여기로 넘어올때 실행
		super.onResume()
		if(ArCoreApk.getInstance().requestInstall(this,true) ==
			ArCoreApk.InstallStatus.INSTALLED  ){
			mSession = Session(this)
			mSession!!.resume()
		}
		myGLView!!.onResume()
	}

	override fun onPause() {
		super.onPause()

		mSession!!.pause()
		myGLView!!.onPause()
		if (!isFirstObjCreate && isrun){
			myGLRenderer!!.pause = true
			timer?.cancel()
			val dialog = PauseFragment()
			dialog.show(supportFragmentManager, "PauseFragment")
		}
	}

	fun preRender(){
		// Log.d("preRender 여","gogo")

		if(myGLRenderer!!.viewportChange){	//화면이 변환되었다면
			val display = windowManager.defaultDisplay //회전상태 확인

			//세션의 화면 정보 갱신
			mSession!!.setDisplayGeometry(display.rotation, myGLRenderer!!.width, myGLRenderer!!.height)
			myGLRenderer!!.viewportChange = false //화면 변환 해제

		}

		//이미 실제 카메라를 세션에서 적용
		// 렌더러에서 사용하도록 지정 --> CameraRenderer로 사용하도록 ID 설정
		mSession!!.setCameraTextureName(myGLRenderer!!.textureID)

		try {
			frame = mSession!!.update()
		}catch (e:Exception){
			e.printStackTrace()
		}
		if(frame!=null) {  //frame이 null 이 되는 경우가 있어서 null이 아닐때만 실행
			myGLRenderer!!.mCamera.transformDisplayGeometry(frame!!)
		}

		var mViewMatrix = FloatArray(16)
		var mProjMatrix = FloatArray(16)

		//frame의 camera의 viewMatrix ==> mViewMatrix에 대입입
		frame!!.camera.getViewMatrix(mViewMatrix,0)

		//frame의 camera의 projectionMatrix ==> mProjMatrix 대입입    near     far
		frame!!.camera.getProjectionMatrix(mProjMatrix,0, 0.01f, 100f)

		myGLRenderer!!.updateViewMatrix(mViewMatrix)
		myGLRenderer!!.updateProjMatrix(mProjMatrix)


		if (isFirstObjCreate) {
			// 중앙 좌표값 받아오기
			var hitResults = frame!!.hitTest((myGLView!!.width / 2f),(myGLView!!.height / 2f))

			// (게임시작)카메라 실행시 최초 한번, 표적의 중심이 될 좌표값을 구하고 랜덤위치에 오브젝트들 생성
			if (isFirstObjCreate && hitResults.isNotEmpty() && !myGLRenderer!!.pause) {
				isFirstObjCreate = false
				for (hr in hitResults) { // 해당 히트리절트의 값들을 전부 돈다
					senterPose = hr.hitPose // 히트리절트의 좌표값
					senterPoseX = senterPose!!.tx()
					senterPoseY = senterPose!!.ty()
					if (senterPose!!.tz() < 0)	senterPoseZ = -3f // Z축 위치가 음수면 음수로 적용
					else						senterPoseZ = 3f

					senterMatrix = FloatArray(16)
					Matrix.setIdentityM(senterMatrix, 0) // 0,0,0로 초기화
					Matrix.translateM(senterMatrix,0, senterPoseX, senterPoseY,senterPoseZ)
					// senterMatrix를 받아온 중앙좌표값으로 설정
					Log.d("if 들어왔 여", "${senterMatrix}")

					var moveAreaCenter = FloatArray(4)
					Matrix.multiplyMV(moveAreaCenter,0,senterMatrix,0, floatArrayOf(0f,0f,0f,1f),0)

					for (obj in myGLRenderer!!.objs) { //오브젝트들 소환
						obj.objMoveArea = moveAreaCenter  // obj 스테이지 중심값을 받아온 중앙좌표값으로 설정
						Log.d("실행됐 여", "${moveAreaCenter}")

						obj.isObjAlive = true
						obj.senterPose = senterPose
						obj.senterPoseZ = senterPoseZ

						obj.randomlyCreate() // 랜덤위치로 생성
					}
				}
				timefun()
			}
		}
	}

	var ms = 0
	var score = 0
	var record = 0
	var isrun = false
	var timer: Timer? = null

	fun timefun() {
		timer = timer(period = 1) {
			ms++
			score++
			runOnUiThread {
				gametimer!!.setProgress(gametimer!!.max - ms)
			}
			isrun = true
			Log.d("테스트 : ","${ms}")
//			scoreTxt!!.text = "${score / 100}초생존"
			if (ms == 20000) {
				cancel()
				ms = 0
				runOnUiThread {
					gametimer!!.setProgress(20000)
				}
				victory = false
				isrun = false
				myGLRenderer!!.pause = true
				val dialog = ResultFragment()
				dialog.show(supportFragmentManager, "ResultFragment")
			}
		}
		return
	}
	override fun onBackPressed() {
		myGLRenderer!!.pause = true
		timer?.cancel()
		val dialog = PauseFragment()
		dialog.show(supportFragmentManager, "PauseFragment")
	}

	fun Num(v: View) {
		val btn =v as Button

		when (btn.id) {
			plus1!!.id -> {
				num += 1
			}
			plus10!!.id -> {
				num += 10
			}
			minus1!!.id -> {
				if (num > 0) {
					num -= 1
				} else {
					num = 0
				}
			}
			minus10!!.id -> {
				if (num > 10) {
					num -= 10
				} else {
					num = 0
				}
			}
		}
		runOnUiThread { txtMyNum!!.text = num.toString() }
	}


}