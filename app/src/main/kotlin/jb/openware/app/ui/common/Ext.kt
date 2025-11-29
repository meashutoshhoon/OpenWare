package jb.openware.app.ui.common

import jb.openware.app.util.PreferenceUtil.getBoolean
import jb.openware.app.util.PreferenceUtil.getInt
import jb.openware.app.util.PreferenceUtil.getString

inline val String.booleanState
    get() = this.getBoolean()

inline val String.stringState
    get() = this.getString()

inline val String.intState
    get() = this.getInt()