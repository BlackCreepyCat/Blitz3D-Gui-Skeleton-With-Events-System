; -------------------------
; Public: Creates a button
; -------------------------
Function CreateButton.GuiWidget(Parent.GuiWidget, Px#, Py#, Sx#, Sy#, Label$)
    Parent.GuiWidget = Parent : If Parent = Null Then Return Null
    
    Widget.GuiWidget = New GuiWidget
    
    Widget\Px = Px
    Widget\Py = Py
    
    Widget\Sx = Sx
    Widget\Sy = Sy
    
    Widget\Label = Label
    
    Widget\WidgetType = Gui_WidgetTypeButton
    
    Widget\Parent = Parent
    Widget\Depth = Parent\Depth
    Widget\Clicked = False
	
    ; Add the child to the parent
    If Parent\ChildCount < 100 Then
        Parent\Children[Parent\ChildCount] = Widget
        Parent\ChildCount = Parent\ChildCount + 1
    End If
    
    Return Widget
End Function


; --------------------------
; Public: Creates a checkbox
; --------------------------
Function CreateCheckbox.GuiWidget(Parent.GuiWidget, Px#, Py#, Sx#, Label$, State)
    Parent.GuiWidget = Parent : If Parent = Null Then Return Null
    
    Widget.GuiWidget = New GuiWidget
    
    Widget\Px = Px
    Widget\Py = Py
    
    Widget\Sx = Sx
    Widget\Sy = 25
    
    Widget\Label = Label
    Widget\State = State  ; Fixed: Assign State to the State field, not Label
    
    Widget\WidgetType = Gui_WidgetTypeCheckbox
    
    Widget\Parent = Parent
    Widget\Depth = Parent\Depth
    Widget\Clicked = False
    
    ; Add the child to the parent
    If Parent\ChildCount < 100 Then
        Parent\Children[Parent\ChildCount] = Widget
        Parent\ChildCount = Parent\ChildCount + 1
    End If
    
    Return Widget
End Function

; -------------------------------------
; Internal: Function to update controls
; -------------------------------------
Function Gui_RefreshWidget()
    Local HoveredWidget.GuiWidget = Null    ; Currently hovered widget
    Local absX#, absY#                      ; Variables to avoid repeated calls
	
    ; Check if there is an active modal window
    Local hasModal = False
    Local modalWindow.GuiWidget = Null
    For Widget.GuiWidget = Each GuiWidget
        If Widget\WidgetType = Gui_WidgetTypeWindow And Widget\Modal And Widget\Active Then
            hasModal = True
            modalWindow = Widget
            Exit
        End If
    Next
	
    ; Process all interactive widgets
    For Widget.GuiWidget = Each GuiWidget
		
        If Widget\WidgetType = Gui_WidgetTypeButton Or Widget\WidgetType = Gui_WidgetTypeCheckbox Then
            absX# = GetAbsoluteX(Widget)
            absY# = GetAbsoluteY(Widget)
			
            ; Handle interactions
            Local interaction = HandleWidgetInteraction(Widget, absX#, absY#, hasModal, modalWindow)
			
            ; Apply type-specific behavior based on interaction
            Select interaction
                Case 3  ; Release
					
					
					; Example for automatic window closing
					If Widget\WidgetType = Gui_WidgetTypeButton Then
						
						; If the click come from the window close gadget
						If Widget = Widget\parent\CloseButton Then 
							DeleteWidget(Widget\parent)
							
							
						EndIf
						
						
					End If	
					
				Case 2
					
                    If Widget\WidgetType = Gui_WidgetTypeCheckbox Then
                        Widget\State = 1 - Widget\State ; Toggle checkbox state
                    End If					
					
                Case 1  ; Hover
					
                    HoveredWidget = Widget
					
            End Select
        End If
    Next
	
    ; Reset hover if nothing is detected
    If HoveredWidget = Null Then Gui_LastHoveredWidget = Null
End Function



; -------------------------------------
; Internal: Handle widget interactions
; -------------------------------------
Function HandleWidgetInteraction(Widget.GuiWidget, absX#, absY#, hasModal, modalWindow.GuiWidget)
    Local interactionState = 0  ; 0 = none, 1 = hover, 2 = click, 3 = release
    
    ; Check if the widget is in a modal context
    If (Not hasModal) Or (hasModal And Widget\Parent = modalWindow) Then
        
        If Gui_TestZone(absX, absY, Widget\Sx, Widget\Sy, False, False) Then
            
            ; Hover logic
            If Widget\Depth = Gui_Widget_HighestDepth - 1 And Widget\Clicked = False Then
                Widget\Hovered = True
                If Gui_LastHoveredWidget <> Widget Then
                    Gui_CreateEvent(Gui_WidgetStateHover, Widget)
                    Gui_LastHoveredWidget = Widget
                End If
                interactionState = 1
            End If
            
            ; Click logic (initial click or re-entering while pressed)
            If (Gui_MouseClickLeft Or (Gui_MousePressLeft And Widget\Clicked = False)) And Widget\Depth = Gui_Widget_HighestDepth - 1 Then
                If Gui_CurrentSelectedWindow = Null Or Widget\Depth = Gui_CurrentSelectedWindow\Depth Then
                    Widget\Clicked = True
                    Gui_CreateEvent(Gui_WidgetStateClicked, Widget)
                    interactionState = 2
                End If
            End If
            
            ; Release logic
            If Widget\Clicked And Gui_MouseReleaseLeft Then
                Widget\Clicked = False
                Gui_CreateEvent(Gui_WidgetStateReleased, Widget)
                interactionState = 3
            End If
            
        Else
            ; Reset Clicked state if mouse is outside the widget while still pressed
            If Widget\Clicked And Gui_MousePressLeft Then
                Widget\Clicked = False
            End If
            
            Widget\Hovered = False
            
        End If
        
    End If
    
    Return interactionState
End Function

; -------------------------------------
; Internal: Function to redraw controls
; -------------------------------------
Function Gui_RedrawWidget(Widget.GuiWidget)
	
    absX# = GetAbsoluteX(Widget)
    absY# = GetAbsoluteY(Widget)
	
    Select Widget\WidgetType
			
        Case Gui_WidgetTypeButton
			
            If Widget\Clicked Then
                Gui_Rect(absX, absY, Widget\Sx, Widget\Sy, 1, 100, 150, 200, 0)
            ElseIf Widget\Hovered Then
                Gui_Rect(absX, absY, Widget\Sx, Widget\Sy, 1, 100, 250, 100, 0)
            Else
                Gui_Rect(absX, absY, Widget\Sx, Widget\Sy, 1, 200, 200, 200, 0)
            End If
			
            Gui_Text(absX + 4, absY + 2, Widget\Label, 0, 0, 0)
			
        Case Gui_WidgetTypeCheckbox
			
            ; Simple checkbox rendering
            Gui_Rect(absX, absY, Widget\Sx, Widget\Sy, 1, 200, 200, 200, 0) ; Background
			
            If Widget\State Then
                Gui_Rect(absX + 2, absY + 2, Widget\Sx - 4, Widget\Sy - 4, 1, 0, 0, 0) ; Check mark
            End If
			
            If Widget\Hovered Then
                Gui_Rect(absX, absY, Widget\Sx, Widget\Sy, 0, 100, 250, 100, 0) ; Hover outline
            End If
			
            Gui_Text(absX + Widget\Sx + 4, absY + 2, Widget\Label, 0, 0, 0)
			
    End Select
End Function
;~IDEal Editor Parameters:
;~C#Blitz3D