This test is an example of another way to write load test, the main assets are :

 * Detailed event-driven philosophy "what should i do when i receive this sort of message ???"
 * Very flexible dipatching (scneario dispatcher) to the handlers (onXxxxxx scenarios)
 * Each scenario is smaller, thus it is easier to write, understand, and debug
 * At runtime, the scenario only handles one message, so it's possible to fail it with the least possible consequencies

THE REGISTER CALLFLOW (roughly) (trigerred by loaderRegister and loaderRegisterInit)

 ==> REGISTER
 <== 401
 ==> REGISTER (with-credentials)
 <== 200

THE CALL CALLFLOW (roughly) (trigerred by loaderRegister and loaderRegisterInit)

 ==> OPTION
 <== 200
 ==> INVITE
 <== 407
 ==> INVITE (with-credentials)
 <== 100
 <== 180
 <== 200
 ==> ACK
 ==> MESSAGE
 <== 200
 ==> BYE
 <== 200

HOW TO USE

One your kamelio is setted up and test.xml configured, you might want to start in load mode :

 - loaderRegisterInit
 - onOther
 - on401Or407Response

This will create XXXX listenpoints (one per user) and send one register for each.

Then to really start the callflow, you should start in load mode :

 - loaderRegister
 - loaderOption
 - all onXxxxx testcases