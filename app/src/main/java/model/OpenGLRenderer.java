package model;


import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private float[] rot_quat = new float[] {0.0f,0.0f,0.0f,0.0f};
    private Cube cube;


    public OpenGLRenderer()
    {
        cube = new Cube();

    }

    public void setRotationQuaternion(float[] quat)
    {
        rot_quat = quat;

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        gl.glEnable(GL10.GL_DEPTH_TEST);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        float fovy = 50.0f; // Field of view angle, in degrees, in the Y direction.
        float aspect = (float)width / (float)height;
        float zNear = 0.1f;
        float zFar = 100.0f;
        GLU.gluPerspective(gl, fovy, aspect, zNear, zFar);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

    }


    private float[] quaternion2AxisAngle(float[] q)
    {
        float[] aa = new float[4];
        float angle_rad = (float) Math.acos(q[0]) * 2;
        aa[0] =  angle_rad * 180.0f / ((float) Math.PI);
        aa[1] =  q[1] / ((float) Math.sin(angle_rad/2));
        aa[2]= q[2] / ((float)Math.sin(angle_rad/2));
        aa[3] = q[3] / ((float) Math.sin(angle_rad/2));
        Log.d("quaternion to axis",Float.toString(aa[0])+" "+Float.toString(aa[1])+" "+Float.toString(aa[2])+" "+Float.toString(aa[3]));
        return aa;
    }


    @Override
    public void onDrawFrame(GL10 gl)
    {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glClearColor(1.0f, 1.0f, 1.0f,1.0f);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, -6.0f);
        float aa[] = quaternion2AxisAngle(rot_quat);
        gl.glRotatef(aa[0], aa[1],aa[2], aa[3] );
        cube.draw(gl);


    }

}
