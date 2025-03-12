Global Gui_MouseX%
Global Gui_MouseY%
Global Gui_MouseZ%

Global Gui_MousePressLeft%
Global Gui_MouseClickLeft%
Global Gui_MouseReleaseLeft%
Global Gui_MouseOldLeft%

Global Gui_MouseClickLeft_X%
Global Gui_MouseClickLeft_Y%

Global Gui_MouseReleaseLeft_X%
Global Gui_MouseReleaseLeft_Y%

Global Gui_MousePressRight%
Global Gui_MouseClickRight%
Global Gui_MouseReleaseRight%
Global Gui_MouseOldRight%

Global Gui_MouseClickRight_X%
Global Gui_MouseClickRight_Y%

Global Gui_DblClickTimer.GuiTimer
Global Gui_MouseDblClick%

Global Gui_MouseOldZ%
Global Gui_MouseWheel%

Global Gui_MousePointer% 
Global Gui_MouseIsVisible% = True

; -----------------------------------------
; Internal : Refresh the gui mouse controls
; -----------------------------------------
Function Gui_RefreshMouse(Debug% = True)
	Gui_MouseX=MouseX()
	Gui_MouseY=MouseY()
	Gui_MouseZ=MouseZ()
	
	; ----------------
	; Test mouse wheel
	; ----------------
	If Gui_MouseOldZ=Gui_MouseZ Then
		Gui_MouseWheel=0
	EndIf

	If Gui_MouseOldZ<Gui_MouseZ Then
		Gui_MouseWheel=-1
		Gui_MouseOldZ=Gui_MouseZ
	EndIf

	If Gui_MouseOldZ>Gui_MouseZ Then
		Gui_MouseWheel=1
		Gui_MouseOldZ=Gui_MouseZ
	EndIf	
	
	; ----------------
	; Test Left Button
	; ----------------
	If MouseHit(1) Then
		Gui_MouseClickLeft = True
		Gui_MouseClickLeft_X = Gui_MouseX
		Gui_MouseClickLeft_Y= Gui_MouseY
		
		Gui_MouseReleaseLeft_X=-1
		Gui_MouseReleaseLeft_Y=-1
		
	Else
		Gui_MouseClickLeft = False
	EndIf

	Gui_MouseOldLeft = Gui_MousePressLeft
	Gui_MousePressLeft = MouseDown(1)
	
	; ---------------------
	; Emulate mouse release
	; ---------------------
	If Gui_MouseOldLeft And Gui_MousePressLeft=False Then
		Gui_MouseReleaseLeft = True 
		
		Gui_MouseClickLeft_X = -1
		Gui_MouseClickLeft_Y = -1
		
		Gui_MouseReleaseLeft_X=Gui_MouseX
		Gui_MouseReleaseLeft_Y=Gui_MouseY
	Else
		Gui_MouseReleaseLeft = False
	EndIf

	; -----------------
	; Test Right Button
	; -----------------
	If MouseHit(2) Then
		Gui_MouseClickRight = True
		Gui_MouseClickRight_X = Gui_MouseX
		Gui_MouseClickRight_Y= Gui_MouseY
	Else
		Gui_MouseClickRight = False
	EndIf

	Gui_MouseOldRight = Gui_MousePressRight
	Gui_MousePressRight = MouseDown(2)
	
	; ---------------------
	; Emulate mouse release
	; ---------------------
	If Gui_MouseOldRight And Gui_MousePressRight=False Then
		Gui_MouseReleaseRight = True
		
		Gui_MouseClickRight_X = -1
		Gui_MouseClickRight_Y = -1
	Else
		Gui_MouseReleaseRight = False
	EndIf
	
	; -----------------
	; Doubleclick check
	; -----------------
	If Gui_MouseReleaseLeft Then

		If Gui_TimeOut( Gui_DblClickTimer) = False And Gui_MouseDblClick=1 Then
			
			
			Gui_MouseDblClick=Gui_MouseDblClick +1
		Else If Gui_MouseDblClick=0 Or Gui_MouseDblClick=1Then
			Gui_DblClickTimer = Gui_SetTimer(295 )
			Gui_MouseDblClick=1
		Else
			Gui_MouseDblClick=1
		EndIf
		
		
	EndIf

	; -----------------------
	; How to use double click
	; -----------------------
	If Gui_MouseDblClick=2  Then
		; ---------
	   	; Important
	   	; ---------
		Gui_MouseDblClick=0
	EndIf
	
	; -----------
	; Debug Lines
	; -----------
	If Debug% = True Then
		Color(255,200,100)
		Line (Gui_MouseX , 0 , Gui_MouseX , GraphicsHeight() )
		Line (0 , Gui_MouseY , GraphicsWidth() , Gui_MouseY)
	EndIf
	
	; ------------
	; Draw pointer
	; ------------	
	;If Gui_MouseIsVisible = True Then
	;	Gui_DrawImageRectEx%(Gui_MousePointer%, Gui_MouseX  , Gui_MouseY , 30 , 30 )
	;EndIf
	
End Function

; -----------------------------
; Internal : Init Mouse Pointer
; -----------------------------
Function Gui_InitMouse()
	;Gui_MousePointer% = Gui_LoadImageFromData("Pointer_A")
	;HidePointer
	
End Function

; --------------------------------------------
; Internal : Test If the mouse is under a zone
; --------------------------------------------
Function Gui_TestZone(Px , Py , Sx , Sy , Click=False , Debug=False)		

	If Gui_MouseX>=Px And Gui_MouseX<=Px+Sx And Gui_MouseY>=Py And Gui_MouseY<=Py+Sy Then
		
		If Debug=True Then
			Color(50,255,50)
			Rect (Px - 2  , Py -2 , Sx + 4 , Sy + 4 , False)
		EndIf
		
		If Not Click Then
			Return True
		Else
			If Gui_MouseClickLeft_X>=Px And Gui_MouseClickLeft_X<=(Px+Sx)-1 And Gui_MouseClickLeft_Y>=Py And Gui_MouseClickLeft_Y<=(Py+Sy)-1 Then
				Return True
			Else
				Return False
			EndIf
		EndIf
		
	Else
		Return False
	EndIf
	
End Function
