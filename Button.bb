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
    Local HoveredWidget.GuiWidget = Null 	; Widget actuellement survol�
    Local ClickedWidget.GuiWidget = Null 	; Bouton cliqu�
	
    Local ClickedDepth = -1             	; Profondeur du bouton cliqu�
    Local absX#, absY#                  	; Variables pour �viter appels r�p�t�s

    ; ----------------------------------------------------------------
    ; �tape 1 : Parcours unique pour g�rer survol, clic et rel�chement
    ; ----------------------------------------------------------------
    For Widget.GuiWidget = Each GuiWidget
        ; ------------------------------
        ; V�rifie uniquement les boutons
        ; ------------------------------
        If widget\widgetType = Gui_WidgetTypeButton Then
		
            absX# = GetAbsoluteX(Widget)
            absY# = GetAbsoluteY(Widget)
			
            ; ------------------------------------------------------------------------------------------
            ; V�rifie la zone et aussi si le bouton appartient � la fen�tre active (profondeur maximale)
            ; ------------------------------------------------------------------------------------------
            If Gui_TestZone(absX , absY , widget\Sx , widget\Sy , False , False) Then

				; ------------------------------------------------------------
                ; Survol (seulement si pas cliqu� et � la profondeur maximale)
 				; ------------------------------------------------------------			
                If Widget\Depth = Gui_Widget_HighestDepth - 1 And Widget\Clicked = False Then
				
                    HoveredWidget = Widget
					Widget\Hovered = True
					
                    ; ----------------------
                    ; Nouveau survol d�tect�
                    ; ----------------------
                    If HoveredWidget <> Gui_LastHoveredWidget Then  
                        Gui_CreateEvent(Gui_WidgetStateHover, HoveredWidget)       ; Cr�e un �v�nement de survol

					
                        Gui_LastHoveredWidget = HoveredWidget
                    End If
					
                End If

				; -----------------
                ; Clic (Mouse Down)
				; -----------------
                If Gui_MouseClickLeft Then
				
                    ; --------------------------------------------------------------------
                    ; V�rifie la zone et aussi si c'est un bouton a la profondeur maximale
                    ; --------------------------------------------------------------------
                    If Widget\Depth > ClickedDepth Then
					
                        If Gui_CurrentSelectedWindow = Null Or Widget\depth = Gui_CurrentSelectedWindow\depth Then
                            ClickedWidget = Widget
                            ClickedDepth = Widget\depth
                        End If
						
                    End If
					
                End If

			Else
			
				Widget\Hovered = False
				
            End If

			; ----------------------
            ; Rel�chement (Mouse Up)
			; ----------------------
            If Widget\clicked And Gui_MouseReleaseLeft Then
			
                ;If Gui_TestZone(absX , absY , widget\Sx , widget\Sy , False , False) Then
				If Widget\Hovered = True
				
                    Gui_CreateEvent(Gui_WidgetStateReleased, Widget)      ; Cr�e un �v�nement de rel�chement
                    Return True
                Else
                    Widget\Clicked = False      ; Annule si rel�ch� hors du bouton
                End If
				
            End If
        End If
    Next
	
    ; ------------------------------------------------
    ; R�initialisation du survol si rien n'est d�tect�
    ; ------------------------------------------------
    If HoveredWidget = Null Then Gui_LastHoveredWidget = Null

    ; ----------------------------------------
    ; �tape 2 : Gestion des clics (Mouse Down)
    ; ----------------------------------------
    If ClickedWidget <> Null Then
        ; ----------------------------------------------------------
        ; Marque le bouton comme cliqu� et Cr�e un �v�nement de clic
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
	
		Gui_Rect(absX, absY, widget\Sx, widget\Sy, 1, 100, 150, 200, 0)
		
	ElseIf Widget\Hovered Then
	
		Gui_Rect(absX, absY, widget\Sx, widget\Sy, 1, 100, 250, 100, 0)
		
	Else
	
		Gui_Rect(absX, absY, widget\Sx, widget\Sy, 1, 200, 200, 200, 0)
		
	End If


	Gui_Text(absX + 4, absY + 2, widget\label ,0,0,0)
	
End Function