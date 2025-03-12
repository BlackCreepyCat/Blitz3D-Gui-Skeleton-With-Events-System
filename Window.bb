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
;    Local PotentialWindow.GuiWidget = Null     ; Fen�tre potentiellement sous le curseur
;    Local absX#, absY#                         ; Variables pour �viter appels r�p�t�s
;
;    ; R�initialise la fen�tre s�lectionn�e au d�but
;    Gui_CurrentSelectedWindow = Null
;
;    ; ----------------------------------------------------------------------------
;    ; �tape 1 : D�termine quelle fen�tre est sous le curseur pour les interactions
;    ; ----------------------------------------------------------------------------
;    For Widget.GuiWidget = Each GuiWidget
;        ; -------------------------------
;        ; V�rifie uniquement les fen�tres
;        ; -------------------------------
;        If Widget\WidgetType = Gui_WidgetTypeWindow Then
;            absX# = GetAbsoluteX(Widget)
;            absY# = GetAbsoluteY(Widget)
;			
;            If Gui_TestZone(absX , absY , Widget\Sx , Widget\Sy , False , False) Then
;			
;                ; --------------------------------------------------
;                ; Garde la fen�tre avec la profondeur la plus �lev�e
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
;    ; D�finit la fen�tre s�lectionn�e uniquement si une fen�tre est sous le curseur
;    Gui_CurrentSelectedWindow = PotentialWindow
;
;     ; ----------------------------------------
;    ; �tape 2 : Gestion des clics (Mouse Down)
;     ; ----------------------------------------
;    If Gui_MouseClickLeft Then
;	
;        ; --------------------------------------------------------------
;        ; D�placement ou redimensionnement de la fen�tre sous le curseur
;        ; --------------------------------------------------------------
;        If Gui_CurrentSelectedWindow <> Null Then
;		
;            absX# = GetAbsoluteX(Gui_CurrentSelectedWindow) ; R�utilis� plus loin
;            absY# = GetAbsoluteY(Gui_CurrentSelectedWindow)
;			
;            ; --------------------------------------------------------------------------
;            ; V�rifie si le clic est sur le gadget de redimensionnement (coin bas-droit)
;            ; --------------------------------------------------------------------------
;            If Gui_TestZone(absX + Gui_CurrentSelectedWindow\Sx - Gui_WindowSizeIcon , absY + Gui_CurrentSelectedWindow\Sy - Gui_WindowSizeIcon , Gui_WindowSizeIcon , Gui_WindowSizeIcon , False , False) Then
;                Gui_CurrentSizedWindow = Gui_CurrentSelectedWindow
;				
;                resizeOffsetX = absX + Gui_CurrentSelectedWindow\Sx - Gui_MouseX
;                resizeOffsetY = absY + Gui_CurrentSelectedWindow\Sy - Gui_MouseY
;				
;            ; -----------------------------------------------------------------
;            ; V�rifie si le clic est dans la barre de titre pour le d�placement
;            ; -----------------------------------------------------------------
;            ElseIf Gui_MouseY% < absY + Gui_WindowTitleHeight Then
;			
;                Local closeButtonX# = Gui_CurrentSelectedWindow\Sx - 20
;                Local closeButtonY# = 20
;				
;                ; -------------------------------------------------------------
;                ; Active le d�placement si le clic n'est pas pr�s du bouton "X"
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
;            ; Met la fen�tre cliqu�e au premier plan
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
;    ; �tape 3 : Gestion continue du d�placement
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
;    ; �tape 4 : Gestion continue du redimensionnement
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
;            ; Repositionne le bouton "X" dans le coin sup�rieur droit
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
    ; �tape 1 : D�termine quelle fen�tre est sous le curseur pour les interactions
	; ----------------------------------------------------------------------------
    For Widget.GuiWidget = Each GuiWidget

		; -------------------------------
		; V�rifie uniquement les fen�tres
		; -------------------------------
        If Widget\WidgetType = Gui_WidgetTypeWindow Then
		
            absX# = GetAbsoluteX(Widget)
            absY# = GetAbsoluteY(Widget)
			
			If Gui_TestZone(absX , absY , Widget\Sx , Widget\Sy , False , False) Then

				; --------------------------------------------------
				; Garde la fen�tre avec la profondeur la plus �lev�e
				; --------------------------------------------------
                If Widget\depth > Window_TopDepth Then
				
                    Gui_CurrentSelectedWindow = Widget ; D�finit la fenetre active
                    Window_TopDepth = Widget\depth
					
                End If
				
            End If
			
        End If
    Next

 	; ----------------------------------------
    ; �tape 2 : Gestion des clics (Mouse Down)
 	; ----------------------------------------
    If Gui_MouseClickLeft Then

		; --------------------------------------------------------------
        ; D�placement ou redimensionnement de la fen�tre sous le curseur
		; --------------------------------------------------------------
        If Gui_CurrentSelectedWindow <> Null Then
			
            absX# = GetAbsoluteX(Gui_CurrentSelectedWindow)
            absY# = GetAbsoluteY(Gui_CurrentSelectedWindow)

			; --------------------------------------------------------------------------
            ; V�rifie si le clic est sur le gadget de redimensionnement (coin bas-droit)
			; --------------------------------------------------------------------------
			If Gui_TestZone(absX + Gui_CurrentSelectedWindow\Sx - Gui_WindowSizeIcon , absY + Gui_CurrentSelectedWindow\Sy - Gui_WindowSizeIcon , Gui_WindowSizeIcon , Gui_WindowSizeIcon , False , False) Then
				
                Gui_CurrentSizedWindow = Gui_CurrentSelectedWindow      ; Active le redimensionnement

                Gui_CurrentSelectedWindow\SizeOffsetX# = absX + Gui_CurrentSelectedWindow\Sx - Gui_MouseX ; Calcule le d�calage X
                Gui_CurrentSelectedWindow\SizeOffsetY# = absY + Gui_CurrentSelectedWindow\Sy - Gui_MouseY ; Calcule le d�calage Y

			; -----------------------------------------------------------------
            ; V�rifie si le clic est dans la barre de titre pour le d�placement
			; -----------------------------------------------------------------
            ElseIf Gui_MouseY% < absY + 20 Then
			
                Local closeButtonX# = Gui_CurrentSelectedWindow\Sx - 20   	; Position X du bouton "X"
                Local closeButtonY# = 20 					 				; Taille Y du bouton "X"

				; -------------------------------------------------------------
                ; Active le d�placement si le clic n'est pas pr�s du bouton "X"
				; -------------------------------------------------------------
				If Gui_TestZone(absX  , absY  , closeButtonX ,closeButtonY , False , False) Then
					
                    Gui_CurrentDraggedWindow = Gui_CurrentSelectedWindow      ; Active le d�placement
					
					Gui_CurrentDraggedWindow\DragOffsetX = Gui_MouseX - absX 
					Gui_CurrentDraggedWindow\DragOffsetY = Gui_MouseY - absY 
                End If
				
            End If

 			; --------------------------------------
            ; Met la fen�tre cliqu�e au premier plan
 			; --------------------------------------
            Local OldDepth = Gui_CurrentSelectedWindow\Depth
			
            For widget.GuiWidget = Each GuiWidget
                If widget\depth > OldDepth Then widget\depth = widget\depth - 1 ; D�cale les autres
            Next
			
            Gui_CurrentSelectedWindow\depth = Gui_Widget_HighestDepth - 1  		; Place la fen�tre au sommet
            UpdateChildrenDepth(Gui_CurrentSelectedWindow)      				; Met � jour la profondeur des enfants
        End If

	EndIf
    
	; -----------------------------------------
    ; �tape 3 : Gestion continue du d�placement
	; -----------------------------------------
    If Gui_CurrentDraggedWindow <> Null Then

		; -----------------------------
		; Tant que le clic est maintenu
		; -----------------------------
		If Gui_MousePressLeft Then    
			
            Gui_CurrentDraggedWindow\Px = Gui_MouseX - Gui_CurrentDraggedWindow\DragOffsetX ; Met � jour la position X
            Gui_CurrentDraggedWindow\Py = Gui_MouseY - Gui_CurrentDraggedWindow\DragOffsetY ; Met � jour la position Y
			
        Else
		
            Gui_CurrentDraggedWindow = Null   ; Arr�te le d�placement si rel�ch�
			
        End If
    End If

	; -----------------------------------------------
    ; �tape 4 : Gestion continue du redimensionnement
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
            ; Repositionne le bouton "X" dans le coin sup�rieur droit
			; -------------------------------------------------------
            For i = 0 To Gui_CurrentSizedWindow\childCount - 1
			
                If Gui_CurrentSizedWindow\children[i] <> Null And Gui_CurrentSizedWindow\children[i]\label = "X" Then
                    Gui_CurrentSizedWindow\children[i]\Px = newW - 20
                End If

			Next
        Else
            Gui_CurrentSizedWindow = Null   ; Arr�te le redimensionnement si rel�ch�
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
