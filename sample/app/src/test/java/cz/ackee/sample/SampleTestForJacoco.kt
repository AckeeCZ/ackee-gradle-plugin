package cz.ackee.sample

import org.junit.Assert
import org.junit.Test

class SampleTestForJacoco {

    @Test
    fun add() {
        Assert.assertEquals(2, Sample().add(1, 1))
    }
}
