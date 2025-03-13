; ----------------------------------------
; Name : Gui Skeleton Kernel
; Date : (C)2025
; Site : https://github.com/BlackCreepyCat
; ----------------------------------------
Include "Mouse.bb"
Include "Timer.bb"
Include "Kernel.bb"
Include "Events.bb"
Include "Window.bb"
Include "Button.bb"


Graphics3D 800,600,16,2
SetBuffer BackBuffer()

Global message$ = ""                      ; Temporary message to display
Global messageTimer = 0                   ; Timer for message display duration

Gui_InitGui()

; Creating test widgets to check functionality
win1.GuiWidget = Gui_CreateWindow(100,100,300,200,"Window 1")    	; Window at (100,100), size 300x200
btn1.GuiWidget = CreateButton(win1, 10, 30, 80, 20, "Button 1") 	; Button inside Window 1
btn2.GuiWidget = CreateButton(btn1, 5, 25, 100, 20, "Sub-btn") 		; Child button of Button 1
btn3.GuiWidget = CreateButton(btn2, 5, 25, 100, 20, "SubSub-btn") 	; Child button of Sub-btn
win2.GuiWidget = Gui_CreateWindow(150,150,300,200,"Window 2")    	; Second window
CreateButton(win2, 10, 30, 80, 20, "Test")                  		; Button inside Window 2

win3.GuiWidget = Gui_CreateWindow(250,250,300,200,"MODAL WINDOW, CLOSE ME!",True)

; Main program loop
While Not KeyHit(1) ; Until the Escape key is pressed
	Cls
	RenderWorld
	
	Gui_RefreshWidgets()
    Gui_ProcessEvents()         ; Processes generated events (hover, click, release)	
          
    Gui_DrawMessage()           ; Displays a temporary message if needed

    If win1 <> Null
        Color 0,255,0
        Text 10,40 , "Window 1 X position: " + Str(win1\Px)
    EndIf

    Flip ; Swap buffers to display the rendering
Wend
End

; Processes the event queue
Function Gui_ProcessEvents()

    For ev.GuiEvent = Each GuiEvent   ; Loops through all events
	
        If ev\widget <> Null Then
		
            Select ev\eventType     ; Check event type
			
                Case Gui_WidgetStateHover ; Hover
				
                    message$ = "Hover: " + ev\widget\label    ; Hover message
                    messageTimer = MilliSecs() + 1000         ; Display for 1 second
					
                Case Gui_WidgetStateClicked ; Click
				
                    message$ = "Click on: " + ev\widget\label ; Click message
                    messageTimer = MilliSecs() + 2000         ; Display for 2 seconds
					
                Case Gui_WidgetStateReleased ; Release
				
                    If ev\widget\widgetType = Gui_WidgetTypeButton Then
                        ev\widget\clicked = False ; Reset button state
                    EndIf
					
                    Select ev\widget\label  ; Specific actions based on the button
                        Case "Button 1"
                            message$ = "Button 1 released!"
                            messageTimer = MilliSecs() + 2000
                            win3.GuiWidget = Gui_CreateWindow(Rnd(150,350),Rnd(150,350),300,200,"Window X") 
                        Case "Sub-btn"
                            message$ = "Sub-button released!"
                            messageTimer = MilliSecs() + 2000
                        Default
						
                            message$ = "Released: " + ev\widget\label
                            messageTimer = MilliSecs() + 2000

							; The window automatic close function
							If ev\widget\label = "X" Then  ; Si c'est le bouton "X"
								DeleteWidget(ev\widget\parent) ; Ferme la fenêtre parente
							End If

                    End Select
            End Select
			
        EndIf

    Next

	Gui_PurgeEvent()
	
End Function


; Affiche un message temporaire en haut à gauche
Function Gui_DrawMessage()
    If messageTimer > MilliSecs() Then  ; Si le timer n'est pas écoulé
        Color 255,255,255               ; Fond blanc
        Rect 10, 10, StringWidth(message$) + 20, 20, 1
        Color 0,0,0                     ; Texte noir
        Text 20, 12, message$           ; Affiche le message
    End If
End Function




;~IDEal Editor Parameters:
;~C#Blitz3D