package com.canoo.dolphin.demo

def config = new JavaFxInMemoryConfig()
config.serverDolphin.serverConnector.register new CustomAction(config.serverDolphin.serverModelStore)

MultipleAttributeSwitchView.show(config.clientDolphin)