; -------------------------------------------------------
; Definition of the GuiWidget Type for interface elements
; -------------------------------------------------------
Type GuiWidget
    Field Px#, Py#             		; Coordinates relative to the parent (float)
    Field Sx#, Sy#             		; Width and height (float)
	
    Field MinSx#               		; Minimum Size X for windows
    Field MinSy#               		; Minimum Size Y for windows
	
	Field CloseButton.GuiWidget		; Close button for windows
    
    Field DragOffsetX#
    Field DragOffsetY#
    
    Field SizeOffsetX#
    Field SizeOffsetY#
	
    Field Modal                		; For windows
    Field Sizable              		; For windows
    
    Field Active               		; Active state (not used yet)
    
    Field Hovered              		; Indicates if the mouse is on the widget
    Field Clicked              		; Indicates if the button is clicked
    
    Field State                		; For checkbox
    
    Field Label$               		; Text displayed on the widget
	
    Field WidgetType           		; Type: window, button...
    
    Field Parent.GuiWidget     		; Reference to the parent widget
    Field Children.GuiWidget[100] 	; Array of children (fixed limit of 100)
    Field ChildCount           		; Current number of children
    Field Depth                		; Depth (z-order) for rendering order
End Type

Global Gui_Widget_HighestDepth
Global Gui_LastHoveredWidget.GuiWidget = Null       ; Last hovered widget to avoid repetitions

Global Gui_CurrentSelectedWindow.GuiWidget = Null   ; Widget being selected (window)
Global Gui_CurrentDraggedWindow.GuiWidget = Null    ; Widget being dragged (window)
Global Gui_CurrentSizedWindow.GuiWidget = Null      ; Widget being resized

Global Gui_WidgetTypeWindow = 1234
Global Gui_WidgetTypeButton = 1235
Global Gui_WidgetTypeCheckbox = 1236

Global Gui_WindowTitleHeight = 20
Global Gui_WindowSizeIcon = 10

Global Gui_SystemFont

; --------------------------
; Public: Initialize the GUI
; --------------------------
Function Gui_InitGui()
    Gui_SystemFont% = LoadFont("Arial", 15, True)
End Function

; --------------------------
; Internal: Refresh the GUI
; --------------------------
Function Gui_RefreshWidgets()
	
    ; First, update the widgets
    Gui_RefreshMouse()
    Gui_RefreshWindow()
    Gui_RefreshWidget()
	
    ; Draw all widgets in depth order
    Local Drawn = 0
    Local CurrentDepth = 0
	
    While Drawn < Gui_CountWidgets()
        For Widget.GuiWidget = Each GuiWidget
            If Widget\Depth = CurrentDepth Then
				
                Select Widget\WidgetType
						
					Case Gui_WidgetTypeWindow 
						
						Gui_RedrawWindow(Widget)
						
					Default
						
                        Gui_RedrawWidget(Widget)
						
                End Select
				
                Drawn = Drawn + 1
				
            End If
        Next
		
        CurrentDepth = CurrentDepth + 1
    Wend
	
	
	
End Function

; -------------------------------------------
; Count the total number of existing widgets
; -------------------------------------------
Function Gui_CountWidgets()
    Local count = 0
	
    For widget.GuiWidget = Each GuiWidget
        count = count + 1   ; Increment for each widget
    Next
	
    Return count
End Function

; -----------------------
; Get absolute X position
; -----------------------
Function GetAbsoluteX#(widget.GuiWidget)
    If widget = Null Then Return 0
    If widget\Parent = Null Then Return widget\Px
    
    Return GetAbsoluteX(widget\Parent) + widget\Px
End Function

; -----------------------
; Get absolute Y position
; -----------------------
Function GetAbsoluteY#(widget.GuiWidget)
    If widget = Null Then Return 0
    If widget\Parent = Null Then Return widget\Py
    
    Return GetAbsoluteY(widget\Parent) + widget\Py
End Function

; -------------------------------------------
; Recursively delete all children of a widget
; -------------------------------------------
Function DeleteWidget(widget.GuiWidget)
    If widget = Null Then Return
    
    While widget\ChildCount > 0
        DeleteWidget(widget\Children[widget\ChildCount - 1])
        widget\ChildCount = widget\ChildCount - 1
    Wend
    
    ; Check if this widget is used in a global variable
    If Gui_CurrentDraggedWindow = widget Then Gui_CurrentDraggedWindow = Null
    If Gui_CurrentSizedWindow = widget Then Gui_CurrentSizedWindow = Null
    If Gui_LastHoveredWidget = widget Then Gui_LastHoveredWidget = Null
    
    ; Delete the widget
    Delete widget
End Function

; ---------------------------------------------------
; Recursively update the depth of a widget's children
; ---------------------------------------------------
Function UpdateChildrenDepth(widget.GuiWidget)
    For i = 0 To widget\ChildCount - 1
        
        If widget\Children[i] <> Null Then
            widget\Children[i]\Depth = widget\Depth ; Apply the parent's depth
            UpdateChildrenDepth(widget\Children[i]) ; Recursion for sub-children
        End If
        
    Next
End Function

; -------------------------------------
; Public: Can be modified for FastImage
; -------------------------------------
Function Gui_Rect(Px%, Py%, Sx%, Sy%, Fill%, R%, G%, B%, Style% = 0)
    Select Style%
            
        ; Simple
        Case 0
            Color R%, G%, B% : Rect(Px, Py, Sx, Sy, Fill%)
            
        ; Flat Border with background
        Case 1
            Color 5, 10, 15  : Rect(Px, Py, Sx, Sy, Fill%)
            Color R%, G%, B% : Rect(Px + 1, Py + 1, Sx - 2, Sy - 2, 1)
            Color 40, 50, 60 : Rect(Px + 1, Py + 1, Sx - 2, Sy - 2, 0)
            
        ; 3D Frame
        Case 2
            Color R%/2, G%/2, B%/2 : Rect(Px + 1, Py + 1, Sx, Sy, 0)
            Color R%, G%, B% : Rect(Px, Py, Sx, Sy, 0)
            
        ; Flat Border without background
        Case 3
            Color 40, 50, 60  : Rect(Px, Py, Sx, Sy, Fill%)
            Color R%, G%, B%  : Rect(Px + 1, Py + 1, Sx - 2, Sy - 2, 0)
            
    End Select
End Function

Function Gui_Line(Px%, Py%, Sx%, Sy%, R%, G%, B%, Style% = 0)
    Select Style%
            
        ; Simple
        Case 0
            Color R%, G%, B%
            Line(Px, Py, Sx, Sy)
            
    End Select
End Function

Function Gui_SetViewport(Px%, Py%, Sx%, Sy%)
    Viewport Px%, Py%, Sx%, Sy%
End Function

Function Gui_Oval(Px, Py, Sx, Sy, Fill%, R%, G%, B%, Style% = 0)
    Select Style%
            
        ; Simple
        Case 0
            Color R%, G%, B% : Oval(Px, Py, Sx, Sy, Fill%)
            
        ; Flat Border
        Case 1
            Color R%/2, G%/2, B%/2 : Oval(Px, Py, Sx, Sy, Fill%)
            Color 100, 100, 100 : Oval(Px, Py, Sx, Sy, 0)
            
            Color R%, G%, B% : Oval(Px + (Sx/4), Py + (Sy/4), Sx/2, Sy/2, Fill%)
            
    End Select
End Function

Function Gui_Text(Px, Py, Caption$, R% = 255, G% = 255, B% = 255, Style% = 1)
    SetFont Gui_SystemFont%
    
    Select Style% 
        Case 1
            Color R%, G%, B% : Text(Px, Py, Caption$)
        Case 2
            Color 0, 0, 0 : Text(Px + 1, Py + 1, Caption$)
            Color R%, G%, B% : Text(Px, Py, Caption$)
    End Select
End Function

; --------------------
; Linear interpolation
; --------------------
Function Lerp(start#, End#, t#)
    Return start# + (End# - start#) * t#
End Function
;~IDEal Editor Parameters:
;~F#39#6A#77#81#9F#AD#C7#D2#D6#E7#F6
;~C#Blitz3D