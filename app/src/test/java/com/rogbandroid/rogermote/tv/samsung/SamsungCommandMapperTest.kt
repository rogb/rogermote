package com.rogbandroid.rogermote.tv.samsung

import com.rogbandroid.rogermote.tv.RemoteCommand
import org.junit.Assert.assertEquals
import org.junit.Test

class SamsungCommandMapperTest {
    @Test
    fun mapsEveryMilestoneThreeCommandToOneSamsungKey() {
        val expected = mapOf(
            RemoteCommand.PowerOff to SamsungKey.PowerOff,
            RemoteCommand.VolumeUp to SamsungKey.VolumeUp,
            RemoteCommand.VolumeDown to SamsungKey.VolumeDown,
            RemoteCommand.Mute to SamsungKey.Mute,
            RemoteCommand.Up to SamsungKey.Up,
            RemoteCommand.Down to SamsungKey.Down,
            RemoteCommand.Left to SamsungKey.Left,
            RemoteCommand.Right to SamsungKey.Right,
            RemoteCommand.Enter to SamsungKey.Enter,
            RemoteCommand.Back to SamsungKey.Back,
            RemoteCommand.Home to SamsungKey.Home,
            RemoteCommand.Source to SamsungKey.Source,
            RemoteCommand.Menu to SamsungKey.Menu,
            RemoteCommand.Exit to SamsungKey.Exit,
            RemoteCommand.Guide to SamsungKey.Guide,
            RemoteCommand.ChannelUp to SamsungKey.ChannelUp,
            RemoteCommand.ChannelDown to SamsungKey.ChannelDown,
            RemoteCommand.PreviousChannel to SamsungKey.PreviousChannel,
            RemoteCommand.Number0 to SamsungKey.Number0,
            RemoteCommand.Number1 to SamsungKey.Number1,
            RemoteCommand.Number2 to SamsungKey.Number2,
            RemoteCommand.Number3 to SamsungKey.Number3,
            RemoteCommand.Number4 to SamsungKey.Number4,
            RemoteCommand.Number5 to SamsungKey.Number5,
            RemoteCommand.Number6 to SamsungKey.Number6,
            RemoteCommand.Number7 to SamsungKey.Number7,
            RemoteCommand.Number8 to SamsungKey.Number8,
            RemoteCommand.Number9 to SamsungKey.Number9,
            RemoteCommand.Play to SamsungKey.Play,
            RemoteCommand.Pause to SamsungKey.Pause,
            RemoteCommand.Stop to SamsungKey.Stop,
            RemoteCommand.Rewind to SamsungKey.Rewind,
            RemoteCommand.FastForward to SamsungKey.FastForward,
            RemoteCommand.Info to SamsungKey.Info,
            RemoteCommand.Red to SamsungKey.Red,
            RemoteCommand.Green to SamsungKey.Green,
            RemoteCommand.Yellow to SamsungKey.Yellow,
            RemoteCommand.Blue to SamsungKey.Blue,
        )

        expected.forEach { (command, samsungKey) ->
            assertEquals(samsungKey, SamsungCommandMapper.toSamsungKey(command))
        }
    }

    @Test
    fun usesTheExpectedSamsungProtocolKeyValues() {
        val expected = mapOf(
            SamsungKey.PowerOff to "KEY_POWER",
            SamsungKey.VolumeUp to "KEY_VOLUP",
            SamsungKey.VolumeDown to "KEY_VOLDOWN",
            SamsungKey.Mute to "KEY_MUTE",
            SamsungKey.Up to "KEY_UP",
            SamsungKey.Down to "KEY_DOWN",
            SamsungKey.Left to "KEY_LEFT",
            SamsungKey.Right to "KEY_RIGHT",
            SamsungKey.Enter to "KEY_ENTER",
            SamsungKey.Back to "KEY_RETURN",
            SamsungKey.Home to "KEY_HOME",
            SamsungKey.Source to "KEY_SOURCE",
            SamsungKey.Menu to "KEY_MENU",
            SamsungKey.Exit to "KEY_EXIT",
            SamsungKey.Guide to "KEY_GUIDE",
            SamsungKey.ChannelUp to "KEY_CHUP",
            SamsungKey.ChannelDown to "KEY_CHDOWN",
            SamsungKey.PreviousChannel to "KEY_PRECH",
            SamsungKey.Number0 to "KEY_0",
            SamsungKey.Number1 to "KEY_1",
            SamsungKey.Number2 to "KEY_2",
            SamsungKey.Number3 to "KEY_3",
            SamsungKey.Number4 to "KEY_4",
            SamsungKey.Number5 to "KEY_5",
            SamsungKey.Number6 to "KEY_6",
            SamsungKey.Number7 to "KEY_7",
            SamsungKey.Number8 to "KEY_8",
            SamsungKey.Number9 to "KEY_9",
            SamsungKey.Play to "KEY_PLAY",
            SamsungKey.Pause to "KEY_PAUSE",
            SamsungKey.Stop to "KEY_STOP",
            SamsungKey.Rewind to "KEY_REWIND",
            SamsungKey.FastForward to "KEY_FF",
            SamsungKey.Info to "KEY_INFO",
            SamsungKey.Red to "KEY_RED",
            SamsungKey.Green to "KEY_GREEN",
            SamsungKey.Yellow to "KEY_YELLOW",
            SamsungKey.Blue to "KEY_BLUE",
        )

        expected.forEach { (samsungKey, protocolValue) ->
            assertEquals(protocolValue, samsungKey.protocolValue)
        }
    }
}
