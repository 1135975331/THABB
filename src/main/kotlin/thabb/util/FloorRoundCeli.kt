package thabb.util

import java.lang.IllegalArgumentException

/* 소수 n번째에서 내림/반올림/올림 */

fun floorFrom(value: Double, pos: Int): Double
{
	if(pos < 1)
		throw IllegalArgumentException("\'pos\' should be at least 1.")

	val powOf10 = Math.pow(10.0, pos-1.0)

	return Math.floor(value*powOf10) / powOf10
}

fun roundFrom(value: Double, pos: Int): Double
{
	if(pos < 1)
		throw IllegalArgumentException("\'pos\' should be at least 1.")

	val powOf10 = Math.pow(10.0, pos-1.0)

	return Math.round(value*powOf10) / powOf10
}

fun ceilFrom(value: Double, pos: Int): Double
{
	if(pos < 1)
		throw IllegalArgumentException("\'pos\' should be at least 1.")

	val powOf10 = Math.pow(10.0, pos-1.0)

	return Math.ceil(value*powOf10) / powOf10
}