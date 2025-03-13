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

; -----------------------------------------
; Internal: Function to update the Buttons
; -----------------------------------------
Function Gui_RefreshButton()
    Local HoveredWidget.GuiWidget = Null    ; Currently hovered widget
    Local ClickedWidget.GuiWidget = Null    ; Clicked button
    
    Local ClickedDepth = -1                 ; Depth of the clicked button
    Local absX#, absY#                      ; Variables to avoid repeated calls
	
    ; ----------------------------------------  
    ; Check if there is an active modal window
    ; ----------------------------------------  
    Local hasModal = False
    Local modalWindow.GuiWidget = Null
    
    For Widget.GuiWidget = Each GuiWidget
        If Widget\WidgetType = Gui_WidgetTypeWindow And Widget\Modal And Widget\Active Then
            hasModal = True
            modalWindow = Widget
            Exit
        End If
    Next
    
    ; -------------------------------------------------------
    ; Step 1: Single pass to handle hover, click, and release
    ; -------------------------------------------------------
    For Widget.GuiWidget = Each GuiWidget
        
        ; ------------------
        ; Check only buttons
        ; ------------------
        If Widget\WidgetType = Gui_WidgetTypeButton Then
            
            absX# = GetAbsoluteX(Widget)
            absY# = GetAbsoluteY(Widget)
            
            ; ----------------------------------------------------------------------------------
            ; Check the zone and whether the button belongs to the active window (maximum depth)
            ; ----------------------------------------------------------------------------------
            If Gui_TestZone(absX, absY, Widget\Sx, Widget\Sy, False, False) Then
                
                ; ------------------------------------------------
                ; Hover (only if not clicked and at maximum depth)
                ; ------------------------------------------------
                If Widget\Depth = Gui_Widget_HighestDepth - 1 And Widget\Clicked = False Then
                    If (Not hasModal) Or (hasModal And Widget\Parent = modalWindow) Then
                        HoveredWidget = Widget
                        Widget\Hovered = True
                        
                        ; ------------------
                        ; New hover detected
                        ; ------------------
                        If HoveredWidget <> Gui_LastHoveredWidget Then  
                            Gui_CreateEvent(Gui_WidgetStateHover, HoveredWidget)       ; Create a hover event
                            Gui_LastHoveredWidget = HoveredWidget
                        End If
                    End If
                End If
                
                ; -----------------
                ; Click (Mouse Down)
                ; -----------------
                If Gui_MouseClickLeft Then
                    
                    ; ---------------------------------------------------------
                    ; Check the zone and whether it’s a button at maximum depth
                    ; ---------------------------------------------------------
                    If Widget\Depth > ClickedDepth Then
                        If (Not hasModal) Or (hasModal And Widget\Parent = modalWindow) Then
                            If Gui_CurrentSelectedWindow = Null Or Widget\Depth = Gui_CurrentSelectedWindow\Depth Then
                                ClickedWidget = Widget
                                ClickedDepth = Widget\Depth
                            End If
                        End If
                    End If
                    
                End If
                
            Else
                
                Widget\Hovered = False
                
            End If
            
            ; ------------------
            ; Release (Mouse Up)
            ; ------------------
            If Widget\Clicked And Gui_MouseReleaseLeft Then
                
                If Widget\Hovered = True
                    
                    Gui_CreateEvent(Gui_WidgetStateReleased, Widget)      ; Create a release event
                    Return True
                    
                Else
                    Widget\Clicked = False                                ; Cancel if released outside the button
                End If
                
            End If
        End If
    Next
    
    ; ------------------------------------------------
    ; Reset hover if nothing is detected
    ; ------------------------------------------------
    If HoveredWidget = Null Then Gui_LastHoveredWidget = Null
    
    ; ----------------------------------
    ; Step 2: Handle clicks (Mouse Down)
    ; ----------------------------------
    If ClickedWidget <> Null Then
        
        ; ---------------------------------------------------
        ; Mark the button as clicked and create a click event
        ; ---------------------------------------------------
        ClickedWidget\Clicked = True
        Gui_CreateEvent(Gui_WidgetStateClicked, ClickedWidget)
        
    End If
    
End Function

; -----------------------------------------
; Internal: Function to redraw the Buttons
; -----------------------------------------
Function Gui_RedrawButton(Widget.GuiWidget)
	
    absX# = GetAbsoluteX(Widget)    ; Absolute X position
    absY# = GetAbsoluteY(Widget)    ; Absolute Y position
	
    If Widget\Clicked Then
		
        Gui_Rect(absX, absY, Widget\Sx, Widget\Sy, 1, 100, 150, 200, 0)
        
    ElseIf Widget\Hovered Then
		
        Gui_Rect(absX, absY, Widget\Sx, Widget\Sy, 1, 100, 250, 100, 0)
        
    Else
		
        Gui_Rect(absX, absY, Widget\Sx, Widget\Sy, 1, 200, 200, 200, 0)
        
    End If
	
    Gui_Text(absX + 4, absY + 2, Widget\Label, 0, 0, 0)
    
End Function
;~IDEal Editor Parameters:
;~C#Blitz3D