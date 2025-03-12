; -----------
; Timer class
; -----------
Type GuiTimer
	Field Start%
	Field TimeOut%
End Type

; --------------------------------------
; Public : Usefull to create timed event
; --------------------------------------
Function Gui_SetTimer.GuiTimer(TimeOut)
	This.GuiTimer = New GuiTimer
	This\Start   = MilliSecs() 
	This\TimeOut = This\Start + TimeOut
	
	Return This
End Function

Function Gui_DeleteTimer(Id.GuiTimer)

	If Id <> Null
		Delete Id
		Return True
	EndIf
	
End Function

Function Gui_TimeOut(Id.GuiTimer)

	If Id <> Null
	
		If Id\TimeOut < MilliSecs()
			Delete Id
			Return True
		Else
			Return False
		EndIf
		
	Else
	
		Return True
		
	EndIf
	
End Function