package com.canoo.dolphin.demo

def config = new JavaFxInMemoryConfig()
config.register new CustomAction(config.modelStore)

PushView.show()