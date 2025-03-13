; ----------------------------------------
; Creates a new Window with a close button
; ----------------------------------------
Function Gui_CreateWindow.GuiWidget(Px#, Py#, Sx#, Sy#, Label$, Sizable = True , Modal = False)
    Win.GuiWidget = New GuiWidget
    
    Win\Px# = Px#
    Win\Py# = Py#
    Win\Sx# = Sx#
    Win\Sy# = Sy#
    
    Win\MinSx# = Sx# / 2
    Win\MinSy# = Sy# / 2
    
    Win\Label = Label
    Win\Modal = Modal
    Win\Sizable = Sizable
    
    Win\WidgetType = Gui_WidgetTypeWindow
    
    ; ----------------
    ; Depth management
    ; ----------------
    If Win\Modal Then
        
        ; ------------------------------------------
        ; Place the modal window at the absolute top
        ; ------------------------------------------
        Win\Depth = Gui_Widget_HighestDepth + 1
        Gui_Widget_HighestDepth = Win\Depth + 1
        
    Else
        
        Win\Depth = Gui_Widget_HighestDepth
        Gui_Widget_HighestDepth = Gui_Widget_HighestDepth + 1
        
    End If
    
    Win\Active = True
    
    ; Add the "X" button to close
    btn.GuiWidget = CreateButton(Win, Win\Sx# - 18, 2, 16, 16, "X")
    
    Return Win
End Function

; ------------------------------
; Function to update the Windows
; ------------------------------
Function Gui_RefreshWindow()
    
    ; ----------------------------------------	
    ; Check if there is an active modal window
    ; ----------------------------------------
    Local HasModal = False
    Local ModalWindow.GuiWidget = Null
    
    For Widget.GuiWidget = Each GuiWidget
        If Widget\WidgetType = Gui_WidgetTypeWindow And Widget\Modal And Widget\Active Then
            HasModal = True
            ModalWindow = Widget
            Exit
        End If
    Next
    
    ; ------------------------------
    ; Maximum depth under the cursor
    ; ------------------------------
    Local Window_TopDepth = -1                 
    Local ClickedWindow.GuiWidget = Null ; Actually clicked window
    
    ; -------------------------------------------------------------------
    ; Step 1: Determine which window is under the cursor for interactions
    ; -------------------------------------------------------------------
    For Widget.GuiWidget = Each GuiWidget
        
        ; ------------------
        ; Check only windows
        ; ------------------
        If Widget\WidgetType = Gui_WidgetTypeWindow Then
            
            absX# = GetAbsoluteX(Widget)
            absY# = GetAbsoluteY(Widget)
            
            If Gui_TestZone(absX, absY, Widget\Sx, Widget\Sy, False, False) Then
                
                ; --------------------------------------
                ; Keep the window with the highest depth
                ; --------------------------------------
                If (HasModal And Widget = ModalWindow) Or (Not HasModal) Then
                    
                    If Widget\Depth > Window_TopDepth Then
                        Gui_CurrentSelectedWindow = Widget ; Set the active window
                        Window_TopDepth = Widget\Depth
                    End If
                    
                End If
                
            End If
            
        End If
        
    Next
    
    ; ----------------------------------
    ; Step 2: Handle clicks (Mouse Down)
    ; ----------------------------------
    If Gui_MouseClickLeft Then
        
        ; ------------------------------------------
        ; Move or resize the window under the cursor
        ; ------------------------------------------
        If Gui_CurrentSelectedWindow <> Null Then
            
            absX# = GetAbsoluteX(Gui_CurrentSelectedWindow)
            absY# = GetAbsoluteY(Gui_CurrentSelectedWindow)
            
            ; ----------------------------------------------------------
            ; Check if the initial click is in the window (Click = True)
            ; ----------------------------------------------------------
            If Gui_TestZone(absX, absY, Gui_CurrentSelectedWindow\Sx, Gui_CurrentSelectedWindow\Sy, True, False) Then
                
                ClickedWindow = Gui_CurrentSelectedWindow
                
                ; ----------------------------------------------------------------
                ; Check if the click is on the resize gadget (bottom-right corner)
                ; ----------------------------------------------------------------
                If Gui_CurrentSelectedWindow\Sizable Then ; Added Sizable condition
                    If Gui_TestZone(absX + Gui_CurrentSelectedWindow\Sx - Gui_WindowSizeIcon, absY + Gui_CurrentSelectedWindow\Sy - Gui_WindowSizeIcon, Gui_WindowSizeIcon, Gui_WindowSizeIcon, True, False) Then
                        
                        Gui_CurrentSizedWindow = Gui_CurrentSelectedWindow      									; Enable resizing
                        Gui_CurrentSelectedWindow\SizeOffsetX# = absX + Gui_CurrentSelectedWindow\Sx - Gui_MouseX 	; Calculate X offset
                        Gui_CurrentSelectedWindow\SizeOffsetY# = absY + Gui_CurrentSelectedWindow\Sy - Gui_MouseY 	; Calculate Y offset
                        
                    End If
                End If
                
                ; ---------------------------------------------------
                ; Check if the click is in the title bar for dragging
                ; ---------------------------------------------------
                If Gui_MouseClickLeft_Y < absY + 20 Then
                    
                    Local closeButtonX# = Gui_CurrentSelectedWindow\Sx - 20   									; "X" button X position
                    Local closeButtonY# = 20 					 												; "X" button Y size
                    
                    ; -------------------------------------------------------
                    ; Enable dragging if the click is not near the "X" button
                    ; -------------------------------------------------------
                    If Gui_TestZone(absX + closeButtonX, absY, 16, 16, True, False) = False Then 				; Avoid the "X" button
                        Gui_CurrentDraggedWindow = Gui_CurrentSelectedWindow      								; Enable dragging
                        Gui_CurrentDraggedWindow\DragOffsetX = Gui_MouseX - absX 
                        Gui_CurrentDraggedWindow\DragOffsetY = Gui_MouseY - absY
                    End If
                    
                End If
                
                ; --------------------------------------
                ; Bring the clicked window to the front
                ; --------------------------------------
                
                If Not ClickedWindow\Modal Then ; Only change depth for non-modal windows
                    Local OldDepth = ClickedWindow\Depth
                    
                    For widget.GuiWidget = Each GuiWidget
                        If widget\Depth > OldDepth And widget\Modal = False Then
                            widget\Depth = widget\Depth - 1 ; Shift others except modal windows
                        End If
                    Next
                    
                    ClickedWindow\Depth = Gui_Widget_HighestDepth - 1
                    
                    If HasModal Then ClickedWindow\Depth = ModalWindow\Depth - 1 	; Below the modal
                    
                    UpdateChildrenDepth(ClickedWindow)                  			; Update children depth
                End If
                
            Else
                
                ; -------------------------------------------------------
                ; Reset the current window if click is outside any window
                ; -------------------------------------------------------
                Gui_CurrentSelectedWindow = Null
                
            End If
        Else
            
            ; -----------------------
            ; No valid click detected
            ; -----------------------
            Gui_CurrentSelectedWindow = Null 
            
        End If
        
    EndIf
    
    ; ------------------------------------
    ; Step 3: Allow dragging even if modal
    ; ------------------------------------
    If Gui_CurrentDraggedWindow <> Null Then 
        
        If Gui_MousePressLeft Then
            
            Gui_CurrentDraggedWindow\Px = Gui_MouseX - Gui_CurrentDraggedWindow\DragOffsetX ; Update X position
            Gui_CurrentDraggedWindow\Py = Gui_MouseY - Gui_CurrentDraggedWindow\DragOffsetY ; Update Y position
            
        Else
            
            ; -------------------------
            ; Stop dragging if released
            ; -------------------------
            Gui_CurrentDraggedWindow = Null   
            
        End If
        
    End If
    
    ; ------------------------------------
    ; Step 4: Allow resizing even if modal
    ; ------------------------------------
    If Gui_CurrentSizedWindow <> Null Then 
        
        If Gui_MousePressLeft And Gui_CurrentSizedWindow\Sizable = True Then ; Added Sizable condition    								
            
            absX# = GetAbsoluteX(Gui_CurrentSizedWindow)
            absY# = GetAbsoluteY(Gui_CurrentSizedWindow)
            
            newW# = Gui_MouseX% - absX + Gui_CurrentSizedWindow\SizeOffsetX#   									; Calculate new width
            newH# = Gui_MouseY% - absY + Gui_CurrentSizedWindow\SizeOffsetY#  									; Calculate new height
            
            If newW < Gui_CurrentSizedWindow\MinSx# Then newW = Gui_CurrentSizedWindow\MinSx#           		; Minimum width limit
            If newH < Gui_CurrentSizedWindow\MinSy# Then newH = Gui_CurrentSizedWindow\MinSy#             		; Minimum height limit
            
            Gui_CurrentSizedWindow\Sx = newW                													; Apply new width
            Gui_CurrentSizedWindow\Sy = newH                													; Apply new height
            
            ; -------------------------------------------------
            ; Reposition the "X" button in the top-right corner
            ; -------------------------------------------------
            For i = 0 To Gui_CurrentSizedWindow\ChildCount - 1
                If Gui_CurrentSizedWindow\Children[i] <> Null And Gui_CurrentSizedWindow\Children[i]\Label = "X" Then
                    Gui_CurrentSizedWindow\Children[i]\Px = newW - 20
                End If
            Next
            
        Else
            
            ; -------------------------
            ; Stop resizing if released
            ; -------------------------
            Gui_CurrentSizedWindow = Null   
            
        End If
        
    End If
    
End Function

; ------------------------------
; Function to redraw the Windows
; ------------------------------
Function Gui_RedrawWindow(Widget.GuiWidget)
    absX# = GetAbsoluteX(Widget)    ; Absolute X position
    absY# = GetAbsoluteY(Widget)    ; Absolute Y position
    
    ; Gray for the body
    Gui_Rect(absX, absY, Widget\Sx, Widget\Sy, 1, 100, 100, 100, 0)
    
    ; Title bar color: two states only
    If Widget\Depth = Gui_Widget_HighestDepth - 1 Then
        ; Red if selected (in the foreground)
        Gui_Rect(absX, absY, Widget\Sx, 20, 1, 150, 50, 50, 0)
    Else
        ; Dark gray if not selected (in the background)
        Gui_Rect(absX, absY, Widget\Sx, 20, 1, 50, 50, 50, 0)
    End If
    
    ; White for the text
    Gui_Text(absX + 5, absY + 2, Widget\Label, 255, 255, 255, 1)
    
    ; Light gray for the resize gadget (displayed only if Sizable)
    If Widget\Sizable Then
        Gui_Rect(absX + Widget\Sx - Gui_WindowSizeIcon, absY + Widget\Sy - Gui_WindowSizeIcon, Gui_WindowSizeIcon, Gui_WindowSizeIcon, 1, 100, 160, 100, 0)
    End If
End Function
;~IDEal Editor Parameters:
;~C#Blitz3D