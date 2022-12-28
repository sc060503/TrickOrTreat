package com.example.trickortreat


import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

//GLSurfaceView 를 렌더링하는 클래스
class MyGLRenderer(val mContext: GameMainActivity, needCreateObjNum: Int):GLSurfaceView.Renderer {
	var viewportChange = false

	var obj:ObjRenderer
	var obj2:ObjRenderer
	var objs:MutableList<ObjRenderer>

	var mCamera:CameraRenderer

	var width = 0
	var height = 0
	var pause = false
	var totalKill = 0

	init{
		obj = ObjRenderer(mContext,"andy.obj","andy.png")
		obj2 = ObjRenderer(mContext,"andy.obj","andy.png")
		objs = mutableListOf()
		for (i in 1..needCreateObjNum) {
			objs.add(ObjRenderer(mContext, "Nintendoghost_small.obj", "Ghost.jpg"))
		}

		mCamera = CameraRenderer()

	}


	val textureID:Int
		get() = if(mCamera.mTextures==null) -1 else mCamera.mTextures!![0]

	override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
		GLES30.glEnable(GLES30.GL_DEPTH_TEST)  //3차원 입체감을 제공
		GLES30.glClearColor(1f,0.6f,0.6f,1f)
		Log.d("MyGLRenderer 여","onSurfaceCreated")


		obj.init()
		obj2.init()
		for(oo in objs){
			oo.init()
		}

		mCamera.init()
	}

	//화면크기가 변경시 화면 크기를 가져와 작업
	override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
		this.width = width
		this.height = height

		viewportChange = true

		Log.d("MyGLRenderer 여","onSurfaceChanged")
		GLES30.glViewport(0,0,width,height)
	}

	override fun onDrawFrame(gl: GL10?) {
		//Log.d("MyGLRenderer 여","onDrawFrame")
		GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

		mContext.preRender()  //그릴때 main의 preRender()를 실행한다.
		GLES30.glDepthMask(false)

		mCamera.draw()
		GLES30.glDepthMask(true)

		obj.draw()

		for(oo in objs) {
			if (!pause) {
				oo.draw()
				oo.move()
			}
		}
	}

	fun updateViewMatrix(matrix:FloatArray){
		obj.setViewMatrix(matrix)
		for(oo in objs){
			oo.setViewMatrix(matrix)
		}
	}

	fun updateProjMatrix(matrix:FloatArray){
		obj.setProjectionMatrix(matrix)
		for(oo in objs){
			oo.setProjectionMatrix(matrix)
		}
	}
}