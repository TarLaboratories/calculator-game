package com.calcgame.main;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Should be used to annotate methods that will primarily be used by mods, and may even be unused outside of mods
 */
@Retention(RetentionPolicy.SOURCE)
public @interface ForMods {
}
