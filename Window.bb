; -------------------------------------------------
; Public : Creates a new Window with a close button
; -------------------------------------------------
;Function Gui_CreateWindow.GuiWidget(Px#, Py#, Sx#, Sy#, Label$)
;
;    Win.GuiWidget = New GuiWidget
;	
;    Win\Px# = Px#
;    Win\Py# = Py#
;    Win\Sx# = Sx#
;    Win\Sy# = Sy#
;
;    Win\MinSx#	= Sx# / 2
;    Win\MinSy#	= Sy# / 2
;	
;    Win\Label = label
;	
;    Win\WidgetType = Gui_WidgetTypeWindow
;	
;    Win\depth = Gui_Widget_HighestDepth
;    Gui_Widget_HighestDepth = Gui_Widget_HighestDepth + 1
;	
;    Win\Active = True
;    
;    ; Add a "X" button to close the Window
;    btn.GuiWidget = CreateButton(Win, Win\Sx# - 18, 2, 16, 16, "X")
;    
;    Return Win
;	
;End Function
;
;; -----------------------------------------
;; Internal : Function to update the Windows
;; -----------------------------------------
;Function Gui_RefreshWindow()
;    ; -----------------------------------
;    ; Profondeur maximale sous le curseur
;    ; -----------------------------------
;    Local Window_TopDepth = -1                 
;    Local PotentialWindow.GuiWidget = Null     ; Fenêtre potentiellement sous le curseur
;    Local absX#, absY#                         ; Variables pour éviter appels répétés
;
;    ; Réinitialise la fenêtre sélectionnée au début
;    Gui_CurrentSelectedWindow = Null
;
;    ; ----------------------------------------------------------------------------
;    ; Étape 1 : Détermine quelle fenêtre est sous le curseur pour les interactions
;    ; ----------------------------------------------------------------------------
;    For Widget.GuiWidget = Each GuiWidget
;        ; -------------------------------
;        ; Vérifie uniquement les fenêtres
;        ; -------------------------------
;        If Widget\WidgetType = Gui_WidgetTypeWindow Then
;            absX# = GetAbsoluteX(Widget)
;            absY# = GetAbsoluteY(Widget)
;			
;            If Gui_TestZone(absX , absY , Widget\Sx , Widget\Sy , False , False) Then
;			
;                ; --------------------------------------------------
;                ; Garde la fenêtre avec la profondeur la plus élevée
;                ; --------------------------------------------------
;                If Widget\depth > Window_TopDepth Then
;                    PotentialWindow = Widget
;                    Window_TopDepth = Widget\depth
;                End If
;				
;            End If
;			
;        End If
;    Next
;
;    ; Définit la fenêtre sélectionnée uniquement si une fenêtre est sous le curseur
;    Gui_CurrentSelectedWindow = PotentialWindow
;
;     ; ----------------------------------------
;    ; Étape 2 : Gestion des clics (Mouse Down)
;     ; ----------------------------------------
;    If Gui_MouseClickLeft Then
;	
;        ; --------------------------------------------------------------
;        ; Déplacement ou redimensionnement de la fenêtre sous le curseur
;        ; --------------------------------------------------------------
;        If Gui_CurrentSelectedWindow <> Null Then
;		
;            absX# = GetAbsoluteX(Gui_CurrentSelectedWindow) ; Réutilisé plus loin
;            absY# = GetAbsoluteY(Gui_CurrentSelectedWindow)
;			
;            ; --------------------------------------------------------------------------
;            ; Vérifie si le clic est sur le gadget de redimensionnement (coin bas-droit)
;            ; --------------------------------------------------------------------------
;            If Gui_TestZone(absX + Gui_CurrentSelectedWindow\Sx - Gui_WindowSizeIcon , absY + Gui_CurrentSelectedWindow\Sy - Gui_WindowSizeIcon , Gui_WindowSizeIcon , Gui_WindowSizeIcon , False , False) Then
;                Gui_CurrentSizedWindow = Gui_CurrentSelectedWindow
;				
;                resizeOffsetX = absX + Gui_CurrentSelectedWindow\Sx - Gui_MouseX
;                resizeOffsetY = absY + Gui_CurrentSelectedWindow\Sy - Gui_MouseY
;				
;            ; -----------------------------------------------------------------
;            ; Vérifie si le clic est dans la barre de titre pour le déplacement
;            ; -----------------------------------------------------------------
;            ElseIf Gui_MouseY% < absY + Gui_WindowTitleHeight Then
;			
;                Local closeButtonX# = Gui_CurrentSelectedWindow\Sx - 20
;                Local closeButtonY# = 20
;				
;                ; -------------------------------------------------------------
;                ; Active le déplacement si le clic n'est pas près du bouton "X"
;                ; -------------------------------------------------------------
;                If Gui_TestZone(absX , absY , closeButtonX ,closeButtonY , False , False) Then
;                    Gui_CurrentDraggedWindow = Gui_CurrentSelectedWindow
;					
;                    Gui_CurrentDraggedWindow\DragOffsetX = Gui_MouseX - absX
;                    Gui_CurrentDraggedWindow\DragOffsetY = Gui_MouseY - absY
;                End If
;				
;            End If
;			
;            ; --------------------------------------
;            ; Met la fenêtre cliquée au premier plan
;            ; --------------------------------------
;            Local OldDepth = Gui_CurrentSelectedWindow\Depth
;			
;            For widget.GuiWidget = Each GuiWidget
;                If widget\depth > OldDepth Then widget\depth = widget\depth - 1
;            Next
;			
;            Gui_CurrentSelectedWindow\depth = Gui_Widget_HighestDepth - 1
;            UpdateChildrenDepth(Gui_CurrentSelectedWindow)
;			
;        End If
;    EndIf
;    
;    ; -----------------------------------------
;    ; Étape 3 : Gestion continue du déplacement
;    ; -----------------------------------------
;    If Gui_CurrentDraggedWindow <> Null Then
;        ; -----------------------------
;        ; Tant que le clic est maintenu
;        ; -----------------------------
;        If Gui_MousePressLeft Then    
;            Gui_CurrentDraggedWindow\Px = Gui_MouseX - Gui_CurrentDraggedWindow\DragOffsetX
;            Gui_CurrentDraggedWindow\Py = Gui_MouseY - Gui_CurrentDraggedWindow\DragOffsetY
;        Else
;            Gui_CurrentDraggedWindow = Null
;        End If
;    End If
;
;    ; -----------------------------------------------
;    ; Étape 4 : Gestion continue du redimensionnement
;    ; -----------------------------------------------
;    If Gui_CurrentSizedWindow <> Null Then
;	
;        If Gui_MousePressLeft Then    					; Tant que le clic est maintenu
;            absX# = GetAbsoluteX(Gui_CurrentSizedWindow)
;            absY# = GetAbsoluteY(Gui_CurrentSizedWindow)
;
;             ;  resizeOffsetX = absX + Gui_CurrentSizedWindow\Sx - Gui_MouseX
;             ;   resizeOffsetY = absY + Gui_CurrentSizedWindow\Sy - Gui_MouseY
;			
;            newW# = Gui_MouseX% - absX + resizeOffsetX
;            newH# = Gui_MouseY% - absY + resizeOffsetY
;			
;            If newW < Gui_CurrentSizedWindow\MinSx# Then newW = Gui_CurrentSizedWindow\MinSx#
;            If newH < Gui_CurrentSizedWindow\MinSy# Then newH = Gui_CurrentSizedWindow\MinSy#
;			
;            Gui_CurrentSizedWindow\Sx = newW
;            Gui_CurrentSizedWindow\Sy = newH
;			
;            ; -------------------------------------------------------
;            ; Repositionne le bouton "X" dans le coin supérieur droit
;            ; -------------------------------------------------------
;            For i = 0 To Gui_CurrentSizedWindow\childCount - 1
;                If Gui_CurrentSizedWindow\children[i] <> Null And Gui_CurrentSizedWindow\children[i]\label = "X" Then
;                    Gui_CurrentSizedWindow\children[i]\Px = newW - 20
;                End If
;            Next
;			
;        Else
;            Gui_CurrentSizedWindow = Null
;        End If
;		
;    End If
;End Function
;
;; -----------------------------------------
;; Internal : Function to redraw the Windows
;; -----------------------------------------
;Function Gui_RedrawWindow(Widget.GuiWidget)
;	absX# = GetAbsoluteX(widget)    ; Position X absolue
;	absY# = GetAbsoluteY(widget)    ; Position Y absolue
;
;	; Gris pour le corps
;	Gui_Rect(absX, absY, widget\Sx, widget\Sy , 1 , 100 , 100 , 100  , 0)
;
;	; Bleu pour la barre de titre
;	Gui_Rect(absX, absY, widget\Sx, Gui_WindowTitleHeight , 1 , 100 , 150 , 200  , 0)
;
;	; Blanc pour le texte
;	Gui_Text(absX + 5, absY + 2, widget\label , 255,255,255 , 1)
;
;	; Resize gadget
;	Gui_Rect(absX + widget\Sx - Gui_WindowSizeIcon, absY + widget\Sy - Gui_WindowSizeIcon, Gui_WindowSizeIcon, Gui_WindowSizeIcon , 1 , 100 , 150 , 200  , 0)
;	
;End Function


; ----------------------------------------
; Creates a new Window with a close button
; ----------------------------------------
Function Gui_CreateWindow.GuiWidget(Px#, Py#, Sx#, Sy#, Label$)

    Win.GuiWidget = New GuiWidget
	
    Win\Px# = Px#
    Win\Py# = Py#
    Win\Sx# = Sx#
    Win\Sy# = Sy#

    Win\MinSx#	= Sx# / 2
    Win\MinSy#	= Sy# / 2
	
    Win\Label = label
	
    Win\WidgetType = Gui_WidgetTypeWindow
	
    Win\depth = Gui_Widget_HighestDepth
    Gui_Widget_HighestDepth = Gui_Widget_HighestDepth + 1
	
    Win\Active = True
    
    ; Add a "X" button to close the Window
    btn.GuiWidget = CreateButton(Win, Win\Sx# - 18, 2, 16, 16, "X")
    
    Return Win
	
End Function

; ------------------------------
; Function to update the Windows
; ------------------------------
Function Gui_RefreshWindow()
	; -----------------------------------
	; Profondeur maximale sous le curseur
	; -----------------------------------
    Local Window_TopDepth = -1                 

	; ----------------------------------------------------------------------------
    ; Étape 1 : Détermine quelle fenêtre est sous le curseur pour les interactions
	; ----------------------------------------------------------------------------
    For Widget.GuiWidget = Each GuiWidget

		; -------------------------------
		; Vérifie uniquement les fenêtres
		; -------------------------------
        If Widget\WidgetType = Gui_WidgetTypeWindow Then
		
            absX# = GetAbsoluteX(Widget)
            absY# = GetAbsoluteY(Widget)
			
			If Gui_TestZone(absX , absY , Widget\Sx , Widget\Sy , False , False) Then

				; --------------------------------------------------
				; Garde la fenêtre avec la profondeur la plus élevée
				; --------------------------------------------------
                If Widget\depth > Window_TopDepth Then
				
                    Gui_CurrentSelectedWindow = Widget ; Définit la fenetre active
                    Window_TopDepth = Widget\depth
					
                End If
				
            End If
			
        End If
    Next

 	; ----------------------------------------
    ; Étape 2 : Gestion des clics (Mouse Down)
 	; ----------------------------------------
    If Gui_MouseClickLeft Then

		; --------------------------------------------------------------
        ; Déplacement ou redimensionnement de la fenêtre sous le curseur
		; --------------------------------------------------------------
        If Gui_CurrentSelectedWindow <> Null Then
			
            absX# = GetAbsoluteX(Gui_CurrentSelectedWindow)
            absY# = GetAbsoluteY(Gui_CurrentSelectedWindow)

			; --------------------------------------------------------------------------
            ; Vérifie si le clic est sur le gadget de redimensionnement (coin bas-droit)
			; --------------------------------------------------------------------------
			If Gui_TestZone(absX + Gui_CurrentSelectedWindow\Sx - Gui_WindowSizeIcon , absY + Gui_CurrentSelectedWindow\Sy - Gui_WindowSizeIcon , Gui_WindowSizeIcon , Gui_WindowSizeIcon , False , False) Then
				
                Gui_CurrentSizedWindow = Gui_CurrentSelectedWindow      ; Active le redimensionnement

                Gui_CurrentSelectedWindow\SizeOffsetX# = absX + Gui_CurrentSelectedWindow\Sx - Gui_MouseX ; Calcule le décalage X
                Gui_CurrentSelectedWindow\SizeOffsetY# = absY + Gui_CurrentSelectedWindow\Sy - Gui_MouseY ; Calcule le décalage Y

			; -----------------------------------------------------------------
            ; Vérifie si le clic est dans la barre de titre pour le déplacement
			; -----------------------------------------------------------------
            ElseIf Gui_MouseY% < absY + 20 Then
			
                Local closeButtonX# = Gui_CurrentSelectedWindow\Sx - 20   	; Position X du bouton "X"
                Local closeButtonY# = 20 					 				; Taille Y du bouton "X"

				; -------------------------------------------------------------
                ; Active le déplacement si le clic n'est pas près du bouton "X"
				; -------------------------------------------------------------
				If Gui_TestZone(absX  , absY  , closeButtonX ,closeButtonY , False , False) Then
					
                    Gui_CurrentDraggedWindow = Gui_CurrentSelectedWindow      ; Active le déplacement
					
					Gui_CurrentDraggedWindow\DragOffsetX = Gui_MouseX - absX 
					Gui_CurrentDraggedWindow\DragOffsetY = Gui_MouseY - absY 
                End If
				
            End If

 			; --------------------------------------
            ; Met la fenêtre cliquée au premier plan
 			; --------------------------------------
            Local OldDepth = Gui_CurrentSelectedWindow\Depth
			
            For widget.GuiWidget = Each GuiWidget
                If widget\depth > OldDepth Then widget\depth = widget\depth - 1 ; Décale les autres
            Next
			
            Gui_CurrentSelectedWindow\depth = Gui_Widget_HighestDepth - 1  		; Place la fenêtre au sommet
            UpdateChildrenDepth(Gui_CurrentSelectedWindow)      				; Met à jour la profondeur des enfants
        End If

	EndIf
    
	; -----------------------------------------
    ; Étape 3 : Gestion continue du déplacement
	; -----------------------------------------
    If Gui_CurrentDraggedWindow <> Null Then

		; -----------------------------
		; Tant que le clic est maintenu
		; -----------------------------
		If Gui_MousePressLeft Then    
			
            Gui_CurrentDraggedWindow\Px = Gui_MouseX - Gui_CurrentDraggedWindow\DragOffsetX ; Met à jour la position X
            Gui_CurrentDraggedWindow\Py = Gui_MouseY - Gui_CurrentDraggedWindow\DragOffsetY ; Met à jour la position Y
			
        Else
		
            Gui_CurrentDraggedWindow = Null   ; Arrête le déplacement si relâché
			
        End If
    End If

	; -----------------------------------------------
    ; Étape 4 : Gestion continue du redimensionnement
	; -----------------------------------------------
    If Gui_CurrentSizedWindow <> Null Then
        If Gui_MousePressLeft Then    					; Tant que le clic est maintenu

            absX# = GetAbsoluteX(Gui_CurrentSizedWindow)
            absY# = GetAbsoluteY(Gui_CurrentSizedWindow)
			
            newW# = Gui_MouseX% - absX + Gui_CurrentSizedWindow\SizeOffsetX#   	; Calcule la nouvelle largeur
            newH# = Gui_MouseY% - absY + Gui_CurrentSizedWindow\SizeOffsetY#  	; Calcule la nouvelle hauteur
			
            If newW < Gui_CurrentSizedWindow\MinSx# Then newW = Gui_CurrentSizedWindow\MinSx#           		; Limite minimale de largeur
            If newH < Gui_CurrentSizedWindow\MinSy# Then newH = Gui_CurrentSizedWindow\MinSy#             		; Limite minimale de hauteur
			
            Gui_CurrentSizedWindow\Sx = newW                ; Applique la nouvelle largeur
            Gui_CurrentSizedWindow\Sy = newH                ; Applique la nouvelle hauteur

			; -------------------------------------------------------
            ; Repositionne le bouton "X" dans le coin supérieur droit
			; -------------------------------------------------------
            For i = 0 To Gui_CurrentSizedWindow\childCount - 1
			
                If Gui_CurrentSizedWindow\children[i] <> Null And Gui_CurrentSizedWindow\children[i]\label = "X" Then
                    Gui_CurrentSizedWindow\children[i]\Px = newW - 20
                End If

			Next
        Else
            Gui_CurrentSizedWindow = Null   ; Arrête le redimensionnement si relâché
        End If
    End If

End Function

Function Gui_RedrawWindow(Widget.GuiWidget)
	absX# = GetAbsoluteX(widget)    ; Position X absolue
	absY# = GetAbsoluteY(widget)    ; Position Y absolue

	; Gris pour le corps
	Gui_Rect(absX, absY, widget\Sx, widget\Sy , 1 , 100 , 100 , 100  , 0)

	; Bleu pour la barre de titre
	Gui_Rect(absX, absY, widget\Sx, 20 , 1 , 100 , 150 , 200  , 0)

	; Blanc pour le texte
	Gui_Text(absX + 5, absY + 2, widget\label , 255,255,255 , 1)
	
	Color 150,150,150           ; Gris clair pour le gadget de redimensionnement
	Rect absX + widget\Sx - Gui_WindowSizeIcon, absY + widget\Sy - Gui_WindowSizeIcon, Gui_WindowSizeIcon, Gui_WindowSizeIcon, 1
End Function
