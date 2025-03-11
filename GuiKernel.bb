; ----------------------------------------
; Name : Gui skeleton Kernel With Events
; Date : (C)2025
; Site : https://github.com/BlackCreepyCat
; ----------------------------------------

; Graphics initialization: 800x600 resolution, 16-bit color depth, windowed mode
Graphics 800,600,16,2
SetBuffer BackBuffer() ; Use back buffer for smooth rendering (double buffering)

; Definition of the Event type for handling user events
Type Event
    Field eventType         ; Event type: 0 = hover, 1 = click, 2 = release
    Field widget.GuiWidget  ; Reference to the widget affected by the event
    Field timestamp         ; Timestamp of the event (in milliseconds)
End Type

; Definition of the GuiWidget type for interface elements (windows and buttons)
Type GuiWidget
    Field x#, y#            ; Coordinates relative to parent (floating-point)
    Field w#, h#            ; Width and height (floating-point)
    Field label$            ; Text displayed on the widget
    Field widgetType        ; Type: 0 = window, 1 = button
    Field parent.GuiWidget  ; Reference to the parent widget
    Field children.GuiWidget[100] ; Array of child widgets (fixed limit of 100)
    Field childCount        ; Current number of children
    Field depth             ; Depth (z-order) for rendering order
    Field active            ; Active state (not currently used)
    Field clicked           ; Indicates if the button is clicked
End Type

; Global variables used throughout the program
Global mouseHit1, mouseX, mouseY        ; Left mouse click state and mouse position
Global draggingWidget.GuiWidget = Null  ; Widget currently being dragged (window)
Global dragOffsetX#, dragOffsetY#       ; Offset for dragging
Global resizingWidget.GuiWidget = Null  ; Widget currently being resized
Global resizeOffsetX#, resizeOffsetY#   ; Offset for resizing
Global highestDepth = 0                 ; Current maximum depth (z-order)
Global message$ = ""                    ; Temporary message to display
Global messageTimer = 0                 ; Timer for message display duration
Global resizeGadgetSize = 10            ; Size of the resize gadget (bottom-right corner)
Global lastHoveredWidget.GuiWidget = Null ; Last hovered widget to avoid repetition

; Creation of test widgets to verify functionality
win1 = CreateWindow(100,100,300,200,"Window 1")    ; Window at (100,100), size 300x200
btn1 = CreateButton(win1, 10, 30, 80, 20, "Button 1") ; Button inside Window 1
btn2 = CreateButton(btn1, 5, 25, 100, 20, "Sub-btn")  ; Button child of Button 1
btn3 = CreateButton(btn2, 5, 25, 100, 20, "SubSub-btn") ; Button child of Sub-btn
win2 = CreateWindow(150,150,300,200,"Window 2")    ; Second window
CreateButton(win2, 10, 30, 80, 20, "Test")         ; Button inside Window 2

; Main program loop
While Not KeyHit(1) ; Runs until the Escape key is pressed
    mouseHit1 = MouseHit(1) ; Detects a left mouse click
    MouseX = MouseX()       ; Updates mouse X position
    MouseY = MouseY()       ; Updates mouse Y position
	
    UpdateWidgets()         ; Updates widget states (hover, click, drag, etc.)
    DrawWidgets()           ; Draws all widgets on the screen
    DrawMessage()           ; Displays a temporary message if needed
	
    ProcessEvents()         ; Processes generated events (hover, click, release)
	
    Flip                    ; Swaps buffers to display the rendered frame
Wend
End

; --- Event handling functions ---

; Creates a new event
Function CreateEvent(eventType, widget.GuiWidget)
    ; Creates a new event instance
    ev.Event = New Event
    ev\eventType = eventType    ; Sets the type (0, 1, or 2)
    ev\widget = widget          ; Links to the affected widget
    ev\timestamp = MilliSecs()  ; Records the current time
    Return Handle(ev)           ; Returns a handle for the event
End Function

; Processes queued events
Function ProcessEvents()
    For ev.Event = Each Event   ; Loops through all events
		If ev\widget <> Null Then
			Select ev\eventType     ; Based on event type
				Case 0 ; Hover
					message$ = "Hover: " + ev\widget\label    ; Hover message
					messageTimer = MilliSecs() + 1000         ; Displays for 1 second
				Case 1 ; Click
					message$ = "Click on: " + ev\widget\label ; Click message
					messageTimer = MilliSecs() + 2000         ; Displays for 2 seconds
				Case 2 ; Release
					Select ev\widget\label  ; Specific actions based on released button
						Case "Button 1"
							message$ = "Button 1 released!"
							messageTimer = MilliSecs() + 2000
						Case "Sub-btn"
							message$ = "Sub-button released!"
							messageTimer = MilliSecs() + 2000
						Default
							message$ = "Released: " + ev\widget\label
							messageTimer = MilliSecs() + 2000
					End Select
			End Select
		EndIf
		
        Delete ev   ; Deletes the event after processing
		
    Next
End Function

; --- GUI functions ---

; Creates a new window with a close button
Function CreateWindow(x#, y#, w#, h#, label$)
    win.GuiWidget = New GuiWidget   ; Creates a new widget instance
    win\x = x                       ; X position
    win\y = y                       ; Y position
    win\w = w                       ; Width
    win\h = h                       ; Height
    win\label = label               ; Window title text
    win\widgetType = 0              ; Type 0 = window
    win\depth = highestDepth        ; Sets current depth
    highestDepth = highestDepth + 1 ; Increments maximum depth
    win\active = True               ; Marks window as active
	
    ; Adds a "X" button to close the window
    btn = CreateButton(Handle(win), w - 20, 2, 16, 16, "X")
	
    Return Handle(win)              ; Returns a handle for the window
End Function

; Creates a button attached to a parent
Function CreateButton(parentHandle, x#, y#, w#, h#, label$)
    parent.GuiWidget = Object.GuiWidget(parentHandle) ; Retrieves parent via handle
    If parent = Null Then Return -1                   ; Returns -1 if parent doesn't exist
	
    btn.GuiWidget = New GuiWidget   ; Creates a new widget instance
    btn\x = x                       ; X position relative to parent
    btn\y = y                       ; Y position relative to parent
    btn\w = w                       ; Width
    btn\h = h                       ; Height
    btn\label = label               ; Button text
    btn\widgetType = 1              ; Type 1 = button
    btn\parent = parent             ; Links to parent
    btn\depth = parent\depth        ; Inherits parent's depth
    btn\clicked = False             ; Initially not clicked
	
    ; Adds the button to the parent's children array
    If parent\childCount < 100 Then
        parent\children[parent\childCount] = btn
        parent\childCount = parent\childCount + 1
    End If
	
    Return Handle(btn)              ; Returns a handle for the button
End Function

; Calculates absolute X position by traversing the parent hierarchy
Function GetAbsoluteX#(widget.GuiWidget)
    If widget = Null Then Return 0              ; Returns 0 if widget doesn't exist
    If widget\parent = Null Then Return widget\x ; Returns X if no parent
    Return GetAbsoluteX(widget\parent) + widget\x ; Adds parent's position
End Function

; Calculates absolute Y position by traversing the parent hierarchy
Function GetAbsoluteY#(widget.GuiWidget)
    If widget = Null Then Return 0              ; Returns 0 if widget doesn't exist
    If widget\parent = Null Then Return widget\y ; Returns Y if no parent
    Return GetAbsoluteY(widget\parent) + widget\y ; Adds parent's position
End Function

; Updates widget states (hover, click, drag, resize)
Function UpdateWidgets()
    Local topWindow.GuiWidget = Null    ; Window under the cursor
    Local topDepth = -1                 ; Highest depth under the cursor
    Local hoveredWidget.GuiWidget = Null ; Currently hovered widget
	
    ; Step 1: Determine which window is under the cursor for interactions
    For widget.GuiWidget = Each GuiWidget
        If widget\widgetType = 0 Then   ; Checks only windows
            absX# = GetAbsoluteX(widget)
            absY# = GetAbsoluteY(widget)
            If MouseX > absX And MouseX < absX + widget\w And MouseY > absY And MouseY < absY + widget\h Then
                If widget\depth > topDepth Then ; Keeps the window with the highest depth
                    topWindow = widget
                    topDepth = widget\depth
                End If
            End If
        End If
    Next
	
    ; Step 2: Detect button hover (only for the active window)
    For widget.GuiWidget = Each GuiWidget
        If widget\widgetType = 1 Then   ; Checks only buttons
            absX# = GetAbsoluteX(widget)
            absY# = GetAbsoluteY(widget)
            If MouseX > absX And MouseX < absX + widget\w And MouseY > absY And MouseY < absY + widget\h Then
                ; Checks if the button belongs to the active window (highest depth)
                If widget\depth = highestDepth - 1 And widget\clicked = False Then
                    hoveredWidget = widget
                    If hoveredWidget <> lastHoveredWidget Then  ; New hover detected
                        CreateEvent(0, hoveredWidget)           ; Creates a hover event
                        lastHoveredWidget = hoveredWidget       ; Updates last hovered
                    End If
                    Exit ; Exits after finding the topmost button
                End If
            End If
        End If
    Next
    If hoveredWidget = Null Then lastHoveredWidget = Null ; Resets if nothing is hovered
	
    ; Step 3: Handle clicks (Mouse Down)
    If mouseHit1 Then
        ; Sub-step 3.1: Dragging or resizing the window under the cursor
        If topWindow <> Null Then
            absX# = GetAbsoluteX(topWindow)
            absY# = GetAbsoluteY(topWindow)
			
            ; Checks if the click is on the resize gadget (bottom-right corner)
            If MouseX > absX + topWindow\w - resizeGadgetSize And MouseY > absY + topWindow\h - resizeGadgetSize Then
                resizingWidget = topWindow      ; Activates resizing
                resizeOffsetX = absX + topWindow\w - MouseX ; Calculates X offset
                resizeOffsetY = absY + topWindow\h - MouseY ; Calculates Y offset
            ; Checks if the click is in the title bar for dragging
            ElseIf MouseY < absY + 20 Then
                Local closeButtonX# = absX + topWindow\w - 20   ; X position of "X" button
                Local closeButtonY# = absY + 2                  ; Y position of "X" button
                Local safetyZone# = 16 + 4                      ; Safety zone around "X"
				
                ; Activates dragging if click is not near the "X" button
                If MouseX < closeButtonX Or MouseX > closeButtonX + safetyZone Or MouseY < closeButtonY Or MouseY > closeButtonY + safetyZone Then
                    draggingWidget = topWindow      ; Activates dragging
                    dragOffsetX = MouseX - absX     ; Calculates X offset
                    dragOffsetY = MouseY - absY     ; Calculates Y offset
                End If
            End If
			
            ; Brings the clicked window to the foreground
            oldDepth = topWindow\depth
            For widget.GuiWidget = Each GuiWidget
                If widget\depth > oldDepth Then widget\depth = widget\depth - 1 ; Shifts others down
            Next
            topWindow\depth = highestDepth - 1  ; Places window at the top
            UpdateChildrenDepth(topWindow)      ; Updates children's depth
        End If
		
        ; Sub-step 3.2: Detect clicks on buttons
        Local clickedWidget.GuiWidget = Null    ; Clicked button
        Local clickedDepth = -1                 ; Depth of clicked button
		
        For widget.GuiWidget = Each GuiWidget
            absX# = GetAbsoluteX(widget)
            absY# = GetAbsoluteY(widget)
            If MouseX > absX And MouseX < absX + widget\w And MouseY > absY And MouseY < absY + widget\h Then
                If widget\widgetType = 1 And widget\depth > clickedDepth Then
                    If topWindow = Null Or widget\depth = topWindow\depth Then
                        clickedWidget = widget      ; Records the clicked button
                        clickedDepth = widget\depth ; Updates highest depth
                    End If
                End If
            End If
        Next
		
        If clickedWidget <> Null Then
            clickedWidget\clicked = True        ; Marks button as clicked
            CreateEvent(1, clickedWidget)       ; Creates a click event
        End If
    End If
	
    ; Step 4: Handle button release (Mouse Up)
    For widget.GuiWidget = Each GuiWidget
        If widget\clicked Then
            If MouseDown(1) = 0 Then    ; Checks if the click is released
                absX# = GetAbsoluteX(widget)
                absY# = GetAbsoluteY(widget)
                If MouseX > absX And MouseX < absX + widget\w And MouseY > absY And MouseY < absY + widget\h Then
                    If widget\label = "X" Then  ; If it's the "X" button
                        DeleteWidget(widget\parent) ; Closes the parent window
                    End If
                    CreateEvent(2, widget)      ; Creates a release event
					
					Return True
					
                  ;  widget\clicked = False      ; Resets clicked state
					
					
                Else
                    widget\clicked = False      ; Cancels if released outside button
                End If
            End If
        End If
    Next
	
    ; Step 5: Continuous dragging management
    If draggingWidget <> Null Then
        If MouseDown(1) Then    ; While click is held
            draggingWidget\x = MouseX - dragOffsetX ; Updates X position
            draggingWidget\y = MouseY - dragOffsetY ; Updates Y position
        Else
            draggingWidget = Null   ; Stops dragging if released
        End If
    End If
	
    ; Step 6: Continuous resizing management
    If resizingWidget <> Null Then
        If MouseDown(1) Then    ; While click is held
            absX# = GetAbsoluteX(resizingWidget)
            absY# = GetAbsoluteY(resizingWidget)
            newW# = MouseX - absX + resizeOffsetX   ; Calculates new width
            newH# = MouseY - absY + resizeOffsetY   ; Calculates new height
            If newW < 50 Then newW = 50             ; Minimum width limit
            If newH < 50 Then newH = 50             ; Minimum height limit
            resizingWidget\w = newW                 ; Applies new width
            resizingWidget\h = newH                 ; Applies new height
            ; Repositions the "X" button in the top-right corner
            For i = 0 To resizingWidget\childCount - 1
                If resizingWidget\children[i] <> Null And resizingWidget\children[i]\label = "X" Then
                    resizingWidget\children[i]\x = newW - 20
                End If
            Next
        Else
            resizingWidget = Null   ; Stops resizing if released
        End If
    End If
End Function

; Linear interpolation (not used here, but kept)
Function Lerp(start#, end#, t#)
    Return start# + (End# - start#) * t#
End Function

; Recursively updates the depth of a widget's children
Function UpdateChildrenDepth(widget.GuiWidget)
    For i = 0 To widget\childCount - 1
        If widget\children[i] <> Null Then
            widget\children[i]\depth = widget\depth ; Applies parent's depth
            UpdateChildrenDepth(widget\children[i]) ; Recursion for sub-children
        End If
    Next
End Function

; Deletes a widget and its children recursively
Function DeleteWidget(widget.GuiWidget)
    If widget = Null Then Return    ; Nothing to do if widget doesn't exist
    For i = 0 To widget\childCount - 1
        DeleteWidget(widget\children[i])    ; Deletes children first
    Next
    Delete widget   ; Deletes the widget itself
End Function

; Draws all widgets in depth order
Function DrawWidgets()
    Cls     ; Clears the screen
    Local drawn = 0         ; Counter for drawn widgets
    Local currentDepth = 0  ; Current depth for rendering
	
    While drawn < CountWidgets()    ; Until all widgets are drawn
        For widget.GuiWidget = Each GuiWidget
            If widget\depth = currentDepth Then
                absX# = GetAbsoluteX(widget)    ; Absolute X position
                absY# = GetAbsoluteY(widget)    ; Absolute Y position
                If widget\widgetType = 0 Then   ; Drawing windows
                    Color 100,100,100           ; Gray for body
                    Rect absX, absY, widget\w, widget\h, 1
                    Color 0,0,200               ; Blue for title bar
                    Rect absX, absY, widget\w, 20, 1
                    Color 255,255,255           ; White for text
                    Text absX + 5, absY + 2, widget\label
                    Color 150,150,150           ; Light gray for resize gadget
                    Rect absX + widget\w - resizeGadgetSize, absY + widget\h - resizeGadgetSize, resizeGadgetSize, resizeGadgetSize, 1
                ElseIf widget\widgetType = 1 Then   ; Drawing buttons
                    If widget\clicked Then
                        Color 150,150,255       ; Light blue if clicked
                    Else
                        Color 200,200,200       ; Light gray by default
                    End If
                    Rect absX, absY, widget\w, widget\h, 1
                    Color 0,0,0                 ; Black for text
                    Text absX + 4, absY + 2, widget\label
                End If
                drawn = drawn + 1   ; Increments counter
            End If
        Next
        currentDepth = currentDepth + 1 ; Moves to next depth
    Wend
End Function

; Displays a temporary message at the top-left corner
Function DrawMessage()
    If messageTimer > MilliSecs() Then  ; If timer hasn't expired
        Color 255,255,255               ; White background
        Rect 10, 10, StringWidth(message$) + 20, 20, 1
        Color 0,0,0                     ; Black text
        Text 20, 12, message$           ; Displays the message
    End If
End Function

; Counts the total number of existing widgets
Function CountWidgets()
    Local count = 0
    For widget.GuiWidget = Each GuiWidget
        count = count + 1   ; Increments for each widget
    Next
    Return count
End Function
