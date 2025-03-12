; --------------------------------------------------------
; Definition of the Event Type to handle user interactions
; --------------------------------------------------------
Type GuiEvent
    Field eventType         		; Event Type: 0 = hover, 1 = click, 2 = release
    Field widget.GuiWidget  		; Reference to the related widget
    Field timestamp         		; Timestamp of the event (in milliseconds)
End Type

Global Gui_WidgetStateHover	= 2456
Global Gui_WidgetStateClicked = 2457
Global Gui_WidgetStateReleaseed = 2458

; ----------------------------
; Public : Creates a new event
; ----------------------------
Function Gui_CreateEvent(EventType, Widget.GuiWidget)
    Ev.GuiEvent = New GuiEvent
	
    Ev\eventType = eventType    ; Sets the event type (0, 1, or 2)
    Ev\widget = widget          ; Links the event to a widget
    Ev\timestamp = MilliSecs()  ; Records the current time
	
    Return Handle(Ev)           ; Returns an event handle
End Function

; ---------------------------------
; Public : Used to purge the events
; ---------------------------------
Function Gui_PurgeEvent()

	For ev.GuiEvent = Each GuiEvent  
        Delete ev   
    Next
End Function