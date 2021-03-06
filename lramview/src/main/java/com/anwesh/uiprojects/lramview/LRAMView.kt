package com.anwesh.uiprojects.lramview

/**
 * Created by anweshmishra on 08/08/18.
 */
import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val nodes : Int = 5
val SCALE_SPEED : Float = 0.05f

fun Canvas.drawLRAMNode(i : Int, scale : Float, paint : Paint) {
    val h : Float = height.toFloat()
    val w : Float = width.toFloat()
    val hGap : Float = h / nodes
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f)) * 2
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = Color.parseColor("#4CAF50")
    save()
    translate(w/2 - hGap/2, hGap * i + hGap/2)
    drawLine( hGap * sc1, 0f, hGap, 0f, paint)
    drawLine(hGap, hGap * sc2, hGap, hGap * sc1, paint)
    restore()
}

class LRAMView (ctx : Context) : View(ctx) {

    val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += SCALE_SPEED * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }
    }

    data class LRAMNode(var i : Int, val state : State = State()) {

        private var next : LRAMNode? = null

        private var prev : LRAMNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = LRAMNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawLRAMNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LRAMNode {
            var curr : LRAMNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedRAM(var i : Int) {

        private var curr : LRAMNode = LRAMNode(0)

        private var dir : Int = 1

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }
    }

    data class Renderer(var view : LRAMView) {

        private var lram : LinkedRAM = LinkedRAM(0)

        private var animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            lram.draw(canvas, paint)
            animator.animate {
                lram.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            lram.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : LRAMView {
            val view : LRAMView = LRAMView(activity)
            activity.setContentView(view)
            return view
        }
    }
}