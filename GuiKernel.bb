; ----------------------------------------
; Name : Gui Skeleton Kernel
; Date : (C)2025
; Site : https://github.com/BlackCreepyCat
; ----------------------------------------

; Initialize graphics mode: resolution 800x600, 16-bit color, windowed mode
Graphics 800,600,16,2
SetBuffer BackBuffer() ; Use back buffer for smooth rendering (double buffering)

; Definition of the Event type to handle user interactions
Type Event
    Field eventType         ; Event type: 0 = hover, 1 = click, 2 = release
    Field widget.GuiWidget  ; Reference to the related widget
    Field timestamp         ; Timestamp of the event (in milliseconds)
End Type

; Definition of the GuiWidget type for interface elements (windows and buttons)
Type GuiWidget
    Field x#, y#            ; Coordinates relative to the parent (float)
    Field w#, h#            ; Width and height (float)
    
    Field label$            ; Text displayed on the widget
    
    Field widgetType        ; Type: 0 = window, 1 = button
    
    Field parent.GuiWidget  ; Reference to the parent widget
    Field children.GuiWidget[100] ; Array of children (fixed limit of 100)
    Field childCount        ; Current number of children
    Field depth             ; Depth (z-order) for rendering order
    
    Field active            ; Active state (not used yet)
    Field clicked           ; Indicates if the button is clicked
End Type

; Global variables used throughout the program
Global mouseHit1, mouseX, mouseY         ; Mouse left click state and position
Global draggingWidget.GuiWidget = Null   ; Widget being dragged (window)
Global dragOffsetX#, dragOffsetY#        ; Offset for dragging
Global resizingWidget.GuiWidget = Null   ; Widget being resized
Global resizeOffsetX#, resizeOffsetY#    ; Offset for resizing
Global highestDepth = 0                  ; Current maximum depth (z-order)
Global message$ = ""                      ; Temporary message to display
Global messageTimer = 0                   ; Timer for message display duration
Global resizeGadgetSize = 10              ; Size of the resize gadget (bottom-right corner)
Global lastHoveredWidget.GuiWidget = Null ; Last hovered widget to avoid repetitions

; Creating test widgets to check functionality
win1.GuiWidget = CreateWindow(100,100,300,200,"Window 1")    ; Window at (100,100), size 300x200
btn1.GuiWidget = CreateButton(win1, 10, 30, 80, 20, "Button 1") ; Button inside Window 1
btn2.GuiWidget = CreateButton(btn1, 5, 25, 100, 20, "Sub-btn") ; Child button of Button 1
btn3.GuiWidget = CreateButton(btn2, 5, 25, 100, 20, "SubSub-btn") ; Child button of Sub-btn
win2.GuiWidget = CreateWindow(150,150,300,200,"Window 2")    ; Second window
CreateButton(win2, 10, 30, 80, 20, "Test")                  ; Button inside Window 2

; Main program loop
While Not KeyHit(1) ; Until the Escape key is pressed
    mouseHit1 = MouseHit(1) ; Detects a left click
    MouseX = MouseX()       ; Updates mouse X position
    MouseY = MouseY()       ; Updates mouse Y position
    
    UpdateWidgets()         ; Updates widget state (hover, click, drag, etc.)
    DrawWidgets()           ; Draws all widgets on the screen
    DrawMessage()           ; Displays a temporary message if needed
    
    ProcessEvents()         ; Processes generated events (hover, click, release)
    
    If win1 <> Null
        Color 0,255,0
        Text 10,40 , "Window 1 X position: " + Str(win1\x)
    EndIf
	
    Flip ; Swap buffers to display the rendering
Wend
End

; --- Event Handling Functions ---

; Creates a new event
Function CreateEvent(eventType, widget.GuiWidget)
    ev.Event = New Event
    ev\eventType = eventType    ; Sets the event type (0, 1, or 2)
    ev\widget = widget          ; Links the event to a widget
    ev\timestamp = MilliSecs()  ; Records the current time
    Return Handle(ev)           ; Returns an event handle
End Function

; Processes the event queue
Function ProcessEvents()
    For ev.Event = Each Event   ; Loops through all events
        If ev\widget <> Null Then
            Select ev\eventType     ; Check event type
                Case 0 ; Hover
                    message$ = "Hover: " + ev\widget\label    ; Hover message
                    messageTimer = MilliSecs() + 1000         ; Display for 1 second
                Case 1 ; Click
                    message$ = "Click on: " + ev\widget\label ; Click message
                    messageTimer = MilliSecs() + 2000         ; Display for 2 seconds
                Case 2 ; Release
                    If ev\widget\widgetType = 1 Then
                        ev\widget\clicked = False ; Reset button state
                    EndIf
                    Select ev\widget\label  ; Specific actions based on the button
                        Case "Button 1"
                            message$ = "Button 1 released!"
                            messageTimer = MilliSecs() + 2000
                            win3.GuiWidget = CreateWindow(Rnd(150,350),Rnd(150,350),300,200,"Window X") 
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

; --- Widget Management Functions ---

; Creates a new window with a close button
Function CreateWindow.GuiWidget(x#, y#, w#, h#, label$)
    win.GuiWidget = New GuiWidget
    win\x = x
    win\y = y
    win\w = w
    win\h = h
    win\label = label
    win\widgetType = 0
    win\depth = highestDepth
    highestDepth = highestDepth + 1
    win\active = True
    
    ; Add a "X" button to close the window
    btn.GuiWidget = CreateButton(win, w - 20, 2, 16, 16, "X")
    
    Return win
End Function

; Creates a button attached to a parent
Function CreateButton.GuiWidget(parentHandle.GuiWidget, x#, y#, w#, h#, label$)
    parent.GuiWidget = parentHandle
    If parent = Null Then Return Null
    
    btn.GuiWidget = New GuiWidget
    btn\x = x
    btn\y = y
    btn\w = w
    btn\h = h
    btn\label = label
    btn\widgetType = 1
    btn\parent = parent
    btn\depth = parent\depth
    btn\clicked = False
    
    If parent\childCount < 100 Then
        parent\children[parent\childCount] = btn
        parent\childCount = parent\childCount + 1
    End If
    
    Return btn
End Function

; Get absolute X position
Function GetAbsoluteX#(widget.GuiWidget)
    If widget = Null Then Return 0
    If widget\parent = Null Then Return widget\x
    Return GetAbsoluteX(widget\parent) + widget\x
End Function

; Get absolute Y position
Function GetAbsoluteY#(widget.GuiWidget)
    If widget = Null Then Return 0
    If widget\parent = Null Then Return widget\y
    Return GetAbsoluteY(widget\parent) + widget\y
End Function

; More functions for updating, rendering, and handling interactions should be added here...
; Met à jour l'état des widgets (survol, clic, déplacement, redimensionnement)
Function UpdateWidgets()
    Local topWindow.GuiWidget = Null    ; Fenêtre sous le curseur
    Local topDepth = -1                 ; Profondeur maximale sous le curseur
    Local hoveredWidget.GuiWidget = Null ; Widget actuellement survolé
    
    ; Étape 1 : Détermine quelle fenêtre est sous le curseur pour les interactions
    For widget.GuiWidget = Each GuiWidget
        If widget\widgetType = 0 Then   ; Vérifie uniquement les fenêtres
            absX# = GetAbsoluteX(widget)
            absY# = GetAbsoluteY(widget)
			
            If MouseX > absX And MouseX < absX + widget\w And MouseY > absY And MouseY < absY + widget\h Then
                If widget\depth > topDepth Then ; Garde la fenêtre avec la profondeur la plus élevée
                    topWindow = widget
                    topDepth = widget\depth
                End If
            End If
			
        End If
    Next
    
    ; Étape 2 : Détection du survol des boutons (uniquement pour la fenêtre active)
    For widget.GuiWidget = Each GuiWidget
        If widget\widgetType = 1 Then   ; Vérifie uniquement les boutons
            absX# = GetAbsoluteX(widget)
            absY# = GetAbsoluteY(widget)
			
            If MouseX > absX And MouseX < absX + widget\w And MouseY > absY And MouseY < absY + widget\h Then
                ; Vérifie si le bouton appartient à la fenêtre active (profondeur maximale)
                If widget\depth = highestDepth - 1 And widget\clicked = False Then
                    hoveredWidget = widget
                    If hoveredWidget <> lastHoveredWidget Then  ; Nouveau survol détecté
                        CreateEvent(0, hoveredWidget)           ; Crée un événement de survol
                        lastHoveredWidget = hoveredWidget       ; Met à jour le dernier survolé
                    End If
                    Exit ; Sort après avoir trouvé le bouton le plus en avant
                End If
            End If
			
        End If
    Next
	
    If hoveredWidget = Null Then lastHoveredWidget = Null ; Réinitialise si rien n'est survolé
    
    ; Étape 3 : Gestion des clics (Mouse Down)
    If mouseHit1 Then
        ; Sous-étape 3.1 : Déplacement ou redimensionnement de la fenêtre sous le curseur
        If topWindow <> Null Then
            absX# = GetAbsoluteX(topWindow)
            absY# = GetAbsoluteY(topWindow)
            
            ; Vérifie si le clic est sur le gadget de redimensionnement (coin bas-droit)
            If MouseX > absX + topWindow\w - resizeGadgetSize And MouseY > absY + topWindow\h - resizeGadgetSize Then
                resizingWidget = topWindow      ; Active le redimensionnement
                resizeOffsetX = absX + topWindow\w - MouseX ; Calcule le décalage X
                resizeOffsetY = absY + topWindow\h - MouseY ; Calcule le décalage Y
            ; Vérifie si le clic est dans la barre de titre pour le déplacement
            ElseIf MouseY < absY + 20 Then
                Local closeButtonX# = absX + topWindow\w - 20   ; Position X du bouton "X"
                Local closeButtonY# = absY + 2                  ; Position Y du bouton "X"
                Local safetyZone# = 16 + 4                      ; Zone de sécurité autour de "X"
                
                ; Active le déplacement si le clic n'est pas près du bouton "X"
                If MouseX < closeButtonX Or MouseX > closeButtonX + safetyZone Or MouseY < closeButtonY Or MouseY > closeButtonY + safetyZone Then
                    draggingWidget = topWindow      ; Active le déplacement
                    dragOffsetX = MouseX - absX     ; Calcule le décalage X
                    dragOffsetY = MouseY - absY     ; Calcule le décalage Y
                End If
            End If
            
            ; Met la fenêtre cliquée au premier plan
            oldDepth = topWindow\depth
			
            For widget.GuiWidget = Each GuiWidget
                If widget\depth > oldDepth Then widget\depth = widget\depth - 1 ; Décale les autres
            Next
			
            topWindow\depth = highestDepth - 1  ; Place la fenêtre au sommet
            UpdateChildrenDepth(topWindow)      ; Met à jour la profondeur des enfants
        End If
        
        ; Sous-étape 3.2 : Détection des clics sur les boutons
        Local clickedWidget.GuiWidget = Null    ; Bouton cliqué
        Local clickedDepth = -1                 ; Profondeur du bouton cliqué
        
        For widget.GuiWidget = Each GuiWidget
            absX# = GetAbsoluteX(widget)
            absY# = GetAbsoluteY(widget)
            If MouseX > absX And MouseX < absX + widget\w And MouseY > absY And MouseY < absY + widget\h Then
                If widget\widgetType = 1 And widget\depth > clickedDepth Then
                    If topWindow = Null Or widget\depth = topWindow\depth Then
                        clickedWidget = widget      ; Enregistre le bouton cliqué
                        clickedDepth = widget\depth ; Met à jour la profondeur maximale
                    End If
                End If
            End If
        Next
        
        If clickedWidget <> Null Then
            clickedWidget\clicked = True        ; Marque le bouton comme cliqué
            CreateEvent(1, clickedWidget)       ; Crée un événement de clic
        End If
    End If
    
    ; Étape 4 : Gestion du relâchement des boutons (Mouse Up)
    For widget.GuiWidget = Each GuiWidget
        If widget\clicked Then
			
            If MouseDown(1) = 0 Then    ; Vérifie si le clic est relâché
				
                absX# = GetAbsoluteX(widget)
                absY# = GetAbsoluteY(widget)
				
                If MouseX > absX And MouseX < absX + widget\w And MouseY > absY And MouseY < absY + widget\h Then
					
                    If widget\label = "X" Then  ; Si c'est le bouton "X"
                        DeleteWidget(widget\parent) ; Ferme la fenêtre parente
                    End If
					
                    CreateEvent(2, widget)      ; Crée un événement de relâchement
					Return True
					
                Else
					
                    widget\clicked = False      ; Annule si relâché hors du bouton
					
                End If
            End If
			
        End If
    Next
    
    ; Étape 5 : Gestion continue du déplacement
    If draggingWidget <> Null Then
        If MouseDown(1) Then    ; Tant que le clic est maintenu
            draggingWidget\x = MouseX - dragOffsetX ; Met à jour la position X
            draggingWidget\y = MouseY - dragOffsetY ; Met à jour la position Y
        Else
            draggingWidget = Null   ; Arrête le déplacement si relâché
        End If
    End If
    
    ; Étape 6 : Gestion continue du redimensionnement
    If resizingWidget <> Null Then
        If MouseDown(1) Then    ; Tant que le clic est maintenu
            absX# = GetAbsoluteX(resizingWidget)
            absY# = GetAbsoluteY(resizingWidget)
            newW# = MouseX - absX + resizeOffsetX   ; Calcule la nouvelle largeur
            newH# = MouseY - absY + resizeOffsetY   ; Calcule la nouvelle hauteur
            If newW < 50 Then newW = 50             ; Limite minimale de largeur
            If newH < 50 Then newH = 50             ; Limite minimale de hauteur
            resizingWidget\w = newW                 ; Applique la nouvelle largeur
            resizingWidget\h = newH                 ; Applique la nouvelle hauteur
            ; Repositionne le bouton "X" dans le coin supérieur droit
            For i = 0 To resizingWidget\childCount - 1
                If resizingWidget\children[i] <> Null And resizingWidget\children[i]\label = "X" Then
                    resizingWidget\children[i]\x = newW - 20
                End If
            Next
        Else
            resizingWidget = Null   ; Arrête le redimensionnement si relâché
        End If
    End If
End Function

; Interpolation linéaire (non utilisée ici, mais conservée)
Function Lerp(start#, end#, t#)
    Return start# + (End# - start#) * t#
End Function

; Met à jour récursivement la profondeur des enfants d'un widget
Function UpdateChildrenDepth(widget.GuiWidget)
    For i = 0 To widget\childCount - 1
        If widget\children[i] <> Null Then
            widget\children[i]\depth = widget\depth ; Applique la profondeur du parent
            UpdateChildrenDepth(widget\children[i]) ; Récursion pour les sous-enfants
        End If
    Next
End Function


Function DeleteWidget(widget.GuiWidget)
    If widget = Null Then Return
    
    ; Supprime tous les enfants du widget récursivement
    While widget\childCount > 0
        DeleteWidget(widget\children[widget\childCount - 1])
        widget\childCount = widget\childCount - 1
    Wend
	
    ; Vérifie si ce widget est utilisé dans une variable globale
    If draggingWidget = widget Then draggingWidget = Null
    If resizingWidget = widget Then resizingWidget = Null
    If lastHoveredWidget = widget Then lastHoveredWidget = Null
	
    ; Supprime le widget
    Delete widget
End Function


; Dessine tous les widgets dans l'ordre de profondeur
Function DrawWidgets()
    Cls     ; Efface l'écran
    Local drawn = 0         ; Compteur de widgets dessinés
    Local currentDepth = 0  ; Profondeur actuelle pour le rendu
    
    While drawn < CountWidgets()    ; Tant que tous les widgets ne sont pas dessinés
        For widget.GuiWidget = Each GuiWidget
            If widget\depth = currentDepth Then
                absX# = GetAbsoluteX(widget)    ; Position X absolue
                absY# = GetAbsoluteY(widget)    ; Position Y absolue
                If widget\widgetType = 0 Then   ; Dessin des fenêtres
                    Color 100,100,100           ; Gris pour le corps
                    Rect absX, absY, widget\w, widget\h, 1
                    Color 0,0,200               ; Bleu pour la barre de titre
                    Rect absX, absY, widget\w, 20, 1
                    Color 255,255,255           ; Blanc pour le texte
                    Text absX + 5, absY + 2, widget\label
                    Color 150,150,150           ; Gris clair pour le gadget de redimensionnement
                    Rect absX + widget\w - resizeGadgetSize, absY + widget\h - resizeGadgetSize, resizeGadgetSize, resizeGadgetSize, 1
                ElseIf widget\widgetType = 1 Then   ; Dessin des boutons
                    If widget\clicked Then
                        Color 150,150,255       ; Bleu clair si cliqué
                    Else
                        Color 200,200,200       ; Gris clair par défaut
                    End If
                    Rect absX, absY, widget\w, widget\h, 1
                    Color 0,0,0                 ; Noir pour le texte
                    Text absX + 4, absY + 2, widget\label
                End If
                drawn = drawn + 1   ; Incrémente le compteur
            End If
        Next
        currentDepth = currentDepth + 1 ; Passe à la profondeur suivante
    Wend
End Function

; Affiche un message temporaire en haut à gauche
Function DrawMessage()
    If messageTimer > MilliSecs() Then  ; Si le timer n'est pas écoulé
        Color 255,255,255               ; Fond blanc
        Rect 10, 10, StringWidth(message$) + 20, 20, 1
        Color 0,0,0                     ; Texte noir
        Text 20, 12, message$           ; Affiche le message
    End If
End Function

; Compte le nombre total de widgets existants
Function CountWidgets()
    Local count = 0
    For widget.GuiWidget = Each GuiWidget
        count = count + 1   ; Incrémente pour chaque widget
    Next
    Return count
End Function

