package com.android.db.library.base

import org.greenrobot.eventbus.EventBus

/**
 * EventBus @link{.RxJava}
 *
 * Created by DengBo on 08/03/2018.
 */

val defaultEventBus: EventBus = EventBus.getDefault()

open class IEvent

class DummyEvent: IEvent()
