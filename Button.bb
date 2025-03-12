; -------------------------
; Public : Creates a button
; -------------------------
Function CreateButton.GuiWidget(Parent.GuiWidget, Px#, Py#, Sx#, Sy#, Label$)
    Parent.GuiWidget = Parent : If parent = Null Then Return Null
    
    Widget.GuiWidget = New GuiWidget
	
    Widget\Px = Px
    Widget\Py = Py
	
    Widget\Sx = Sx
    Widget\Sy = Sy
	
    Widget\Label = Label
	
    Widget\WidgetType = Gui_WidgetTypeButton
	
    Widget\Parent = Parent
    Widget\Depth = Parent\Depth
    Widget\clicked = False

	; On ajoute l'enfant au parent
    If Parent\ChildCount < 100 Then
        Parent\Children[Parent\ChildCount] = Widget
        Parent\ChildCount = Parent\ChildCount + 1
    End If
    
    Return Widget
End Function

; -----------------------------------------
; Internal : Function to update the Buttons
; -----------------------------------------
Function Gui_RefreshButton()
    Local HoveredWidget.GuiWidget = Null 	; Widget actuellement survolé
    Local ClickedWidget.GuiWidget = Null 	; Bouton cliqué
	
    Local ClickedDepth = -1             	; Profondeur du bouton cliqué
    Local absX#, absY#                  	; Variables pour éviter appels répétés

    ; ----------------------------------------------------------------
    ; Étape 1 : Parcours unique pour gérer survol, clic et relâchement
    ; ----------------------------------------------------------------
    For Widget.GuiWidget = Each GuiWidget
        ; ------------------------------
        ; Vérifie uniquement les boutons
        ; ------------------------------
        If widget\widgetType = Gui_WidgetTypeButton Then
		
            absX# = GetAbsoluteX(Widget)
            absY# = GetAbsoluteY(Widget)
			
            ; ------------------------------------------------------------------------------------------
            ; Vérifie la zone et aussi si le bouton appartient à la fenêtre active (profondeur maximale)
            ; ------------------------------------------------------------------------------------------
            If Gui_TestZone(absX , absY , widget\Sx , widget\Sy , False , False) Then

				; ------------------------------------------------------------
                ; Survol (seulement si pas cliqué et à la profondeur maximale)
 				; ------------------------------------------------------------			
                If Widget\Depth = Gui_Widget_HighestDepth - 1 And Widget\clicked = False Then
				
                    HoveredWidget = Widget
					
                    ; ----------------------
                    ; Nouveau survol détecté
                    ; ----------------------
                    If HoveredWidget <> Gui_LastHoveredWidget Then  
                        Gui_CreateEvent(Gui_WidgetStateHover, HoveredWidget)       ; Crée un événement de survol
						
                        Gui_LastHoveredWidget = HoveredWidget
                    End If
					
                End If

				; -----------------
                ; Clic (Mouse Down)
				; -----------------
                If Gui_MouseClickLeft Then
				
                    ; --------------------------------------------------------------------
                    ; Vérifie la zone et aussi si c'est un bouton a la profondeur maximale
                    ; --------------------------------------------------------------------
                    If Widget\Depth > ClickedDepth Then
                        If Gui_CurrentSelectedWindow = Null Or Widget\depth = Gui_CurrentSelectedWindow\depth Then
                            ClickedWidget = Widget
                            ClickedDepth = Widget\depth
                        End If
                    End If
					
                End If
				
            End If

			; ----------------------
            ; Relâchement (Mouse Up)
			; ----------------------
            If Widget\clicked And Gui_MouseReleaseLeft Then
			
                If Gui_TestZone(absX , absY , widget\Sx , widget\Sy , False , False) Then
                    Gui_CreateEvent(Gui_WidgetStateReleaseed, Widget)      ; Crée un événement de relâchement
                    Return True
                Else
                    Widget\clicked = False      ; Annule si relâché hors du bouton
                End If
				
            End If
        End If
    Next
	
    ; ------------------------------------------------
    ; Réinitialisation du survol si rien n'est détecté
    ; ------------------------------------------------
    If HoveredWidget = Null Then Gui_LastHoveredWidget = Null

    ; ----------------------------------------
    ; Étape 2 : Gestion des clics (Mouse Down)
    ; ----------------------------------------
    If ClickedWidget <> Null Then
        ; ----------------------------------------------------------
        ; Marque le bouton comme cliqué et Crée un événement de clic
        ; ----------------------------------------------------------
        ClickedWidget\Clicked = True
        Gui_CreateEvent(Gui_WidgetStateClicked, ClickedWidget)
    End If
End Function

; -----------------------------------------
; Internal : Function to redraw the Buttons
; -----------------------------------------
Function Gui_RedrawButton(Widget.GuiWidget)

	absX# = GetAbsoluteX(Widget)    ; Position X absolue
	absY# = GetAbsoluteY(Widget)    ; Position Y absolue
	
	If Widget\Clicked Then
		Color 150,150,255       ; Bleu clair si cliqué
	Else
		Color 200,200,200       ; Gris clair par défaut
	End If
	
	Rect absX, absY, widget\Sx, widget\Sy, 1
	
	Color 0,0,0                 ; Noir pour le texte
	Text absX + 4, absY + 2, widget\label
End Function