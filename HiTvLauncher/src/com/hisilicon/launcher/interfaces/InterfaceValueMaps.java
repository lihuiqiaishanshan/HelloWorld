
package com.hisilicon.launcher.interfaces;

import com.hisilicon.android.tvapi.constant.EnumSoundMode;
import com.hisilicon.launcher.R;

/**
 * Parameter of interface
 *
 * @author huyq
 */
public class InterfaceValueMaps {
    /**
     * all configuration settings
     */
    public static final int app_item_values[][] = {
            {
                    R.id.set_item_net, R.string.net_setting
            },
            {
                    R.id.set_item_pic, R.string.pic_setting
            },
            {
                    R.id.set_item_sound, R.string.voice_setting
            },
            {
                    R.id.set_item_sysupgrade, R.string.system
            },
            {
                    R.id.set_item_advanced, R.string.advance_setting
            },
            {
                    R.id.set_item_recover, R.string.recover_setting
            },
            {
                    R.id.set_item_systeminfo, R.string.system_info
            },
            {
                    R.id.set_item_help, R.string.help
            }
    };

    /**
     * sound settings
     */
    public static final int voice_mode_logic[][] = {
            {
                    EnumSoundMode.SNDMODE_STANDARD, R.string.sndmode_standard_string
            },
            {
                    EnumSoundMode.SNDMODE_NEWS, R.string.sndmode_news_string
            },
            {
                    EnumSoundMode.SNDMODE_MUSIC, R.string.sndmode_music_string
            },
            {
                    EnumSoundMode.SNDMODE_MOVIE, R.string.sndmode_movie_string
            },
            {
                    EnumSoundMode.SNDMODE_SPORTS, R.string.sndmode_sport_string
            },
            {
                    EnumSoundMode.SNDMODE_USER, R.string.sndmode_user_string
            }
    };

    /**
     * The language switching
     */
    public static final int language_change[][] = {
            {
                    0, R.string.chinese
            },
            {
                    1, R.string.english
            }
    };

    /**
     * Sleep time
     */
    public static final int system_sleep[][] = {
            {
                    0, R.string.sleep_close
            },
            {
                    1, R.string.five_min
            }, {
                    2, R.string.thirty_min
            },
            {
                    3, R.string.sixty_min
            }, {
                    4, R.string.ninety_min
            },
            {
                    5, R.string.sixscore_min
            }
    };

    /**
     * Power music on/off
     */
    public static final int[][] on_off = { { 0, R.string.off }, { 1, R.string.on } };

    /**
     * Music number on/off
     */
    public static final int[][] music_number = { { 1, R.string.music_1 }, { 2, R.string.music_2 } };
}
