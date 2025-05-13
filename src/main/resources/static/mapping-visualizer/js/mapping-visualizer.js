/**
 * Script principal pour le visualiseur de mappings
 */
document.addEventListener('DOMContentLoaded', function() {
    // Variables globales
    let currentMapping = null;
    let simulation = null;
    const apiBaseUrl = '/api/mappings/visualization';
    let svg = null;
    let g = null;
    let width = 0;
    let height = 0;
    let zoom = null;

    // Initialisation
    init();

    /**
     * Initialise l'application
     */
    function init() {
        console.log("Initialisation de l'application de visualisation des mappings");

        // Charger la liste des mappings
        loadMappingsList();

        // Initialiser les gestionnaires d'événements
        initEventHandlers();
    }

    /**
     * Initialise les gestionnaires d'événements
     */
    function initEventHandlers() {
        // Boutons de zoom et d'exportation
        if (document.getElementById('zoom-in-btn')) {
            document.getElementById('zoom-in-btn').addEventListener('click', zoomIn);
        }

        if (document.getElementById('zoom-out-btn')) {
            document.getElementById('zoom-out-btn').addEventListener('click', zoomOut);
        }

        if (document.getElementById('reset-zoom-btn')) {
            document.getElementById('reset-zoom-btn').addEventListener('click', resetZoom);
        }

        if (document.getElementById('export-svg-btn')) {
            document.getElementById('export-svg-btn').addEventListener('click', exportSVG);
        }

        if (document.getElementById('export-png-btn')) {
            document.getElementById('export-png-btn').addEventListener('click', exportPNG);
        }

        if (document.getElementById('export-dot-btn')) {
            document.getElementById('export-dot-btn').addEventListener('click', exportDOT);
        }

        if (document.getElementById('export-json-btn')) {
            document.getElementById('export-json-btn').addEventListener('click', exportJSON);
        }

        // Bouton d'enregistrement des modifications
        if (document.getElementById('save-mapping-btn')) {
            document.getElementById('save-mapping-btn').addEventListener('click', saveMapping);
        }

        console.log("Gestionnaires d'événements initialisés");
    }

    /**
     * Charge la liste des mappings depuis l'API
     */
    function loadMappingsList() {
        console.log("Chargement de la liste des mappings depuis:", apiBaseUrl);

        fetch(apiBaseUrl)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Erreur HTTP: ${response.status} - ${response.statusText}`);
                }
                return response.json();
            })
            .then(mappings => {
                console.log("Mappings reçus:", mappings);
                displayMappingsList(mappings);
            })
            .catch(error => {
                console.error('Erreur lors du chargement des mappings:', error);

                // Afficher une alerte à l'utilisateur
                const container = document.getElementById('mappings-list');
                if (container) {
                    container.innerHTML = `
                        <div class="col-12">
                            <div class="alert alert-danger" role="alert">
                                <i class="fas fa-exclamation-triangle me-2"></i>
                                Impossible de charger la liste des mappings: ${error.message}
                            </div>
                        </div>
                    `;
                } else {
                    alert('Impossible de charger la liste des mappings: ' + error.message);
                }
            });
    }

    /**
     * Affiche la liste des mappings sous forme de cartes
     */
    function displayMappingsList(mappings) {
        const container = document.getElementById('mappings-list');

        if (!container) {
            console.error("Élément 'mappings-list' introuvable dans le DOM");
            return;
        }

        // Si aucun mapping n'est disponible
        if (!mappings || mappings.length === 0) {
            container.innerHTML = `
                <div class="col-12">
                    <div class="alert alert-info" role="alert">
                        <i class="fas fa-info-circle me-2"></i>
                        Aucun mapping n'est disponible. Veuillez créer des définitions de mapping pour les visualiser.
                    </div>
                </div>
            `;
            return;
        }

        // Supprimer les placeholders
        container.innerHTML = '';

        // Créer une carte pour chaque mapping
        mappings.forEach(mapping => {
            const card = document.createElement('div');
            card.className = 'col';
            card.innerHTML = `
                <div class="card h-100 mapping-card" data-mapping-id="${mapping.id}">
                    <div class="card-body">
                        <h5 class="card-title">${mapping.id}</h5>
                        <h6 class="card-subtitle mb-2 text-muted">Priorité: ${mapping.priority}</h6>
                        <p class="card-text">
                            <strong>Source:</strong> ${getSimpleClassName(mapping.sourceType)}<br>
                            <strong>Cible:</strong> ${getSimpleClassName(mapping.targetType)}<br>
                            <strong>Champs:</strong> ${mapping.fieldMappingCount || (mapping.fieldMappings ? mapping.fieldMappings.length : 0)}
                        </p>
                    </div>
                    <div class="card-footer d-flex justify-content-end">
                        <button class="btn btn-sm btn-primary visualize-btn">
                            <i class="fas fa-project-diagram me-1"></i> Visualiser
                        </button>
                    </div>
                </div>
            `;

            container.appendChild(card);

            // Ajouter un gestionnaire d'événements pour le bouton de visualisation
            const visualizeBtn = card.querySelector('.visualize-btn');
            if (visualizeBtn) {
                visualizeBtn.addEventListener('click', () => {
                    loadMapping(mapping.id);
                });
            }
        });

        console.log(`${mappings.length} mappings affichés dans la liste`);
    }

    /**
     * Charge les détails d'un mapping depuis l'API
     */
    function loadMapping(mappingId) {
        console.log("Chargement du mapping:", mappingId);

        fetch(`${apiBaseUrl}/${mappingId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Erreur HTTP: ${response.status} - ${response.statusText}`);
                }
                return response.json();
            })
            .then(mapping => {
                console.log("Mapping reçu:", mapping);
                currentMapping = mapping;
                displayMappingDetails(mapping);
                loadD3Visualization(mappingId);
            })
            .catch(error => {
                console.error('Erreur lors du chargement du mapping:', error);
                alert('Impossible de charger le mapping: ' + error.message);
            });
    }

    /**
     * Charge les données de visualisation D3 depuis l'API
     */
    function loadD3Visualization(mappingId) {
        console.log("Chargement des données de visualisation pour:", mappingId);

        fetch(`${apiBaseUrl}/${mappingId}/d3`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Erreur HTTP: ${response.status} - ${response.statusText}`);
                }
                return response.json();
            })
            .then(data => {
                console.log("Données de visualisation reçues:", data);

                // Vérification de la structure des données
                if (!data.nodes || !data.links) {
                    console.error("Structure de données incorrecte:", data);
                    alert("Format des données de visualisation invalide");
                    return;
                }

                // Afficher la section de visualisation
                const visualizationSection = document.getElementById('visualization-section');
                if (visualizationSection) {
                    visualizationSection.style.display = 'flex';
                }

                // Mettre à jour le titre
                const visualizationTitle = document.getElementById('visualization-title');
                if (visualizationTitle) {
                    visualizationTitle.innerText = `Visualisation du Mapping: ${mappingId}`;
                }

                // Rendre la visualisation
                renderD3Visualization(data);
            })
            .catch(error => {
                console.error('Erreur lors du chargement des données de visualisation:', error);

                const container = document.getElementById('visualization-container');
                if (container) {
                    container.innerHTML = `
                        <div class="alert alert-danger m-3" role="alert">
                            <i class="fas fa-exclamation-triangle me-2"></i>
                            Impossible de charger les données de visualisation: ${error.message}
                        </div>
                    `;
                } else {
                    alert('Impossible de charger les données de visualisation: ' + error.message);
                }
            });
    }

    /**
     * Affiche les détails du mapping dans le panneau latéral
     */
    function displayMappingDetails(mapping) {
        const container = document.getElementById('mapping-details');

        if (!container) {
            console.error("Élément 'mapping-details' introuvable dans le DOM");
            return;
        }

        let html = `
            <div class="mb-3">
                <strong>ID:</strong> ${mapping.id}
            </div>
            <div class="mb-3">
                <strong>Type Source:</strong> ${mapping.sourceType}
            </div>
            <div class="mb-3">
                <strong>Type Cible:</strong> ${mapping.targetType}
            </div>
            <div class="mb-3">
                <strong>Priorité:</strong> ${mapping.priority}
            </div>
            <div class="mb-3">
                <strong>Mappings de Champs:</strong>
                <ul class="list-group mt-2">
        `;

        // Vérifier si fieldMappings existe et est un tableau
        if (!mapping.fieldMappings || !Array.isArray(mapping.fieldMappings) || mapping.fieldMappings.length === 0) {
            html += `
                <li class="list-group-item text-muted">
                    Aucun mapping de champ défini
                </li>
            `;
        } else {
            mapping.fieldMappings.forEach((fieldMapping, index) => {
                html += `
                    <li class="list-group-item">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <strong>${fieldMapping.sourcePath || '#root'}</strong>
                                <i class="fas fa-arrow-right mx-2"></i>
                                <strong>${fieldMapping.targetPath}</strong>
                            </div>
                            <button class="btn btn-sm btn-outline-primary edit-field-btn" data-index="${index}">
                                <i class="fas fa-edit"></i>
                            </button>
                        </div>
                        ${fieldMapping.transformer ? `<div class="mt-1"><small class="text-muted">Transformer: ${fieldMapping.transformer['@class'] || JSON.stringify(fieldMapping.transformer)}</small></div>` : ''}
                        ${fieldMapping.condition ? `<div><small class="text-muted">Condition: ${fieldMapping.condition['@class'] || JSON.stringify(fieldMapping.condition)}</small></div>` : ''}
                        ${fieldMapping.constant ? `<div><small class="text-muted">Valeur constante: ${JSON.stringify(fieldMapping.constant)}</small></div>` : ''}
                        ${fieldMapping.name ? `<div><small class="text-muted">Nom: ${fieldMapping.name}</small></div>` : ''}
                    </li>
                `;
            });
        }

        html += `
                </ul>
            </div>
            <div class="d-grid gap-2">
                <button class="btn btn-primary" id="edit-mapping-btn">
                    <i class="fas fa-edit me-1"></i> Éditer le Mapping
                </button>
                <button class="btn btn-outline-primary" id="add-field-btn">
                    <i class="fas fa-plus me-1"></i> Ajouter un Champ
                </button>
            </div>
        `;

        container.innerHTML = html;

        // Ajouter des gestionnaires d'événements
        const editMappingBtn = document.getElementById('edit-mapping-btn');
        if (editMappingBtn) {
            editMappingBtn.addEventListener('click', () => showEditMappingModal(mapping));
        }

        const addFieldBtn = document.getElementById('add-field-btn');
        if (addFieldBtn) {
            addFieldBtn.addEventListener('click', () => showAddFieldModal(mapping));
        }

        document.querySelectorAll('.edit-field-btn').forEach(btn => {
            btn.addEventListener('click', (event) => {
                const index = parseInt(event.currentTarget.getAttribute('data-index'), 10);
                if (!isNaN(index) && mapping.fieldMappings[index]) {
                    showEditFieldModal(mapping, mapping.fieldMappings[index], index);
                }
            });
        });

        console.log("Détails du mapping affichés");
    }

    /**
     * Rend la visualisation D3 du mapping
     */
    function renderD3Visualization(data) {
        console.log("Début du rendu D3 avec données:", data);

        const container = document.getElementById('visualization-container');
        if (!container) {
            console.error("Conteneur 'visualization-container' introuvable dans le DOM");
            return;
        }

        // Nettoyer le conteneur
        container.innerHTML = '';

        // Obtenir les dimensions du conteneur
        width = container.clientWidth;
        height = container.clientHeight;

        console.log("Dimensions du conteneur:", width, height);

        // Si le conteneur a une taille nulle, définir des dimensions par défaut
        if (width === 0 || height === 0) {
            console.warn("Le conteneur a une taille nulle, utilisation de dimensions par défaut");
            width = 800;
            height = 600;
            container.style.width = width + 'px';
            container.style.height = height + 'px';
        }

        // Créer l'élément SVG
        svg = d3.select(container)
            .append('svg')
            .attr('width', width)
            .attr('height', height)
            .attr('viewBox', [0, 0, width, height])
            .attr('style', 'max-width: 100%; height: auto; border: 1px solid #ddd;'); // Ajout d'une bordure pour visualiser

        // Ajouter un groupe pour le zoom
        g = svg.append('g');

        // Configurer le zoom
        zoom = d3.zoom()
            .scaleExtent([0.1, 4])
            .on('zoom', (event) => {
                g.attr('transform', event.transform);
            });

        svg.call(zoom);

        // Initialiser les positions des nœuds
        data.nodes.forEach(node => {
            node.x = width / 2 + (Math.random() - 0.5) * width * 0.5;
            node.y = height / 2 + (Math.random() - 0.5) * height * 0.5;
        });

        // Créer les flèches pour les liens
        svg.append('defs').append('marker')
            .attr('id', 'arrowhead')
            .attr('viewBox', '-0 -5 10 10')
            .attr('refX', 20)
            .attr('refY', 0)
            .attr('orient', 'auto')
            .attr('markerWidth', 6)
            .attr('markerHeight', 6)
            .append('path')
            .attr('d', 'M 0,-5 L 10,0 L 0,5')
            .attr('fill', '#2196f3');

        // Créer les liens
        const link = g.append('g')
            .selectAll('path')
            .data(data.links)
            .enter()
            .append('path')
            .attr('class', d => `link link-${d.type}`)
            .attr('marker-end', d => d.type === 'maps_to' ? 'url(#arrowhead)' : null)
            .attr('stroke-width', d => d.type === 'maps_to' ? 2 : 1)
            .attr('stroke', d => {
                // Couleurs par défaut au cas où les classes CSS ne sont pas chargées
                if (d.type === 'maps_to') return '#2196f3';
                return '#9e9e9e';
            });

        // Créer les nœuds
        const node = g.append('g')
            .selectAll('g')
            .data(data.nodes)
            .enter()
            .append('g')
            .attr('class', 'node')
            .call(d3.drag()
                .on('start', dragstarted)
                .on('drag', dragged)
                .on('end', dragended));

        // Ajouter les cercles aux nœuds
        node.append('circle')
            .attr('r', d => d.type === 'class' ? 15 : 10)
            .attr('class', d => `node-${d.group}`)
            .attr('fill', d => {
                // Couleurs par défaut au cas où les classes CSS ne sont pas chargées
                if (d.group === 'source') return '#c8e6c9';
                if (d.group === 'target') return '#bbdefb';
                return '#ffffff';
            })
            .attr('stroke', d => {
                if (d.group === 'source') return '#4caf50';
                if (d.group === 'target') return '#2196f3';
                return '#000000';
            })
            .attr('stroke-width', 1.5);

        // Ajouter les étiquettes aux nœuds
        node.append('text')
            .attr('dx', d => d.type === 'class' ? 20 : 15)
            .attr('dy', 5)
            .text(d => d.name)
            .attr('fill', '#000000') // Couleur du texte pour s'assurer qu'il est visible
            .style('text-shadow', '0 1px 0 #fff, 1px 0 0 #fff, 0 -1px 0 #fff, -1px 0 0 #fff'); // Contour blanc pour améliorer la lisibilité

        // Ajouter des tooltips
        node.append('title')
            .text(d => {
                if (d.type === 'class') return d.id;
                return d.id;
            });

        // Ajouter des informations supplémentaires aux liens
        link.append('title')
            .text(d => {
                if (d.type === 'maps_to') {
                    let title = `${d.source} → ${d.target}`;
                    if (d.transformer) title += `\nTransformateur: ${d.transformer}`;
                    if (d.condition) title += `\nCondition: ${d.condition}`;
                    return title;
                }
                return `${d.source} → ${d.target}`;
            });

        // Configurer la simulation de forces
        simulation = d3.forceSimulation(data.nodes)
            .force('link', d3.forceLink(data.links).id(d => d.id).distance(150)) // Distance augmentée
            .force('charge', d3.forceManyBody().strength(-400)) // Force plus forte
            .force('center', d3.forceCenter(width / 2, height / 2))
            .force('x', d3.forceX(width / 2).strength(0.15))
            .force('y', d3.forceY(height / 2).strength(0.15))
            .force('collision', d3.forceCollide().radius(50))
            .alphaDecay(0.028) // Ralentit la "refroidissement" de la simulation
            .on('tick', () => {
                // Mise à jour des positions des liens
                link.attr('d', d => {
                    // Vérifier que les coordonnées existent
                    if (!d.source || !d.target || !d.source.x || !d.target.x || !d.source.y || !d.target.y) {
                        console.error("Coordonnées manquantes pour le lien:", d);
                        return "";
                    }

                    // Limiter les coordonnées à la zone visible
                    const sourceX = Math.max(20, Math.min(width - 20, d.source.x));
                    const sourceY = Math.max(20, Math.min(height - 20, d.source.y));
                    const targetX = Math.max(20, Math.min(width - 20, d.target.x));
                    const targetY = Math.max(20, Math.min(height - 20, d.target.y));

                    const dx = targetX - sourceX;
                    const dy = targetY - sourceY;
                    const dr = Math.sqrt(dx * dx + dy * dy);

                    return `M${sourceX},${sourceY}A${dr},${dr} 0 0,1 ${targetX},${targetY}`;
                });

                // Mise à jour des positions des nœuds
                node.attr('transform', d => {
                    if (!d.x || !d.y) {
                        console.error("Coordonnées manquantes pour le nœud:", d);
                        return "";
                    }

                    // Limiter les coordonnées à la zone visible avec une marge
                    const x = Math.max(20, Math.min(width - 20, d.x));
                    const y = Math.max(20, Math.min(height - 20, d.y));

                    return `translate(${x},${y})`;
                });
            });

        // Démarrer explicitement la simulation avec une valeur alpha élevée
        simulation.alpha(1).restart();

        // Centrer le graphique
        resetZoom();

        // Ajouter un écouteur de redimensionnement pour adapter le graphique
        window.addEventListener('resize', () => {
            const newWidth = container.clientWidth;
            const newHeight = container.clientHeight;

            if (newWidth !== width || newHeight !== height) {
                console.log("Redimensionnement du conteneur:", newWidth, newHeight);
                width = newWidth;
                height = newHeight;

                svg.attr('width', width)
                    .attr('height', height)
                    .attr('viewBox', [0, 0, width, height]);

                simulation.force('center', d3.forceCenter(width / 2, height / 2))
                    .alpha(0.3)
                    .restart();

                resetZoom();
            }
        });

        // Ajouter un bouton d'interface pour verrouiller/déverrouiller le graphique
        const lockButton = document.createElement('button');
        lockButton.className = 'btn btn-sm btn-outline-secondary position-absolute m-2';
        lockButton.style.zIndex = '100';
        lockButton.style.right = '10px';
        lockButton.style.top = '10px';
        lockButton.innerHTML = '<i class="fas fa-lock-open"></i>';
        lockButton.title = 'Verrouiller/déverrouiller les nœuds';
        let locked = false;

        lockButton.addEventListener('click', () => {
            locked = !locked;
            lockButton.innerHTML = locked ? '<i class="fas fa-lock"></i>' : '<i class="fas fa-lock-open"></i>';

            if (locked) {
                // Fixer les positions des nœuds
                data.nodes.forEach(node => {
                    node.fx = node.x;
                    node.fy = node.y;
                });
            } else {
                // Libérer les positions des nœuds
                data.nodes.forEach(node => {
                    node.fx = null;
                    node.fy = null;
                });

                // Redémarrer la simulation
                simulation.alpha(0.3).restart();
            }
        });

        container.appendChild(lockButton);

        console.log("Rendu D3 terminé");
    }

    /**
     * Fonction déclenchée au début du drag d'un nœud
     */
    function dragstarted(event) {
        if (!event.active) simulation.alphaTarget(0.3).restart();
        event.subject.fx = event.subject.x;
        event.subject.fy = event.subject.y;
    }

    /**
     * Fonction déclenchée pendant le drag d'un nœud
     */
    function dragged(event) {
        event.subject.fx = event.x;
        event.subject.fy = event.y;
    }

    /**
     * Fonction déclenchée à la fin du drag d'un nœud
     */
    function dragended(event) {
        if (!event.active) simulation.alphaTarget(0);
        // Conserver la position fixe après le glisser-déposer
        // event.subject.fx = null;
        // event.subject.fy = null;
    }

    /**
     * Effectue un zoom avant sur la visualisation
     */
    function zoomIn() {
        if (!svg || !zoom) return;
        svg.transition().duration(300).call(zoom.scaleBy, 1.2);
    }

    /**
     * Effectue un zoom arrière sur la visualisation
     */
    function zoomOut() {
        if (!svg || !zoom) return;
        svg.transition().duration(300).call(zoom.scaleBy, 0.8);
    }

    /**
     * Réinitialise le zoom de la visualisation
     */
    function resetZoom() {
        if (!svg || !zoom) return;
        svg.transition().duration(500).call(
            zoom.transform,
            d3.zoomIdentity.translate(width / 2, height / 2).scale(0.8)
        );
    }

    /**
     * Exporte la visualisation au format SVG
     */
    function exportSVG() {
        if (!svg || !currentMapping) return;

        try {
            // Cloner le SVG pour éviter de modifier l'original
            const svgClone = svg.node().cloneNode(true);

            // Ajouter des styles incorporés
            const styleElement = document.createElement('style');
            const cssRules = Array.from(document.styleSheets)
                .filter(sheet => !sheet.href || sheet.href.startsWith(window.location.origin))
                .flatMap(sheet => {
                    try {
                        return Array.from(sheet.cssRules);
                    } catch (e) {
                        console.warn('Impossible d\'accéder aux règles CSS d\'une feuille de style:', e);
                        return [];
                    }
                })
                .filter(rule => rule.selectorText && (
                    rule.selectorText.includes('.node') ||
                    rule.selectorText.includes('.link') ||
                    rule.selectorText.includes('circle') ||
                    rule.selectorText.includes('text')
                ))
                .map(rule => rule.cssText)
                .join('\n');

            styleElement.textContent = cssRules;
            svgClone.insertBefore(styleElement, svgClone.firstChild);

            // Ajouter un fond blanc
            const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
            rect.setAttribute('width', '100%');
            rect.setAttribute('height', '100%');
            rect.setAttribute('fill', 'white');
            svgClone.insertBefore(rect, svgClone.firstChild.nextSibling);

            // Sérialiser le SVG
            const svgData = new XMLSerializer().serializeToString(svgClone);
            const blob = new Blob([svgData], {type: 'image/svg+xml'});
            const url = URL.createObjectURL(blob);

            // Créer un lien de téléchargement
            const link = document.createElement('a');
            link.href = url;
            link.download = `${currentMapping.id}-mapping.svg`;
            link.click();

            URL.revokeObjectURL(url);
        } catch (error) {
            console.error("Erreur lors de l'exportation SVG:", error);
            alert("Impossible d'exporter au format SVG: " + error.message);
        }
    }

    /**
     * Exporte la visualisation au format PNG
     */
    /**
     * Exporte la visualisation au format PNG
     */
    function exportPNG() {
        if (!svg || !currentMapping) return;

        try {
            // Cloner le SVG pour éviter de modifier l'original
            const svgClone = svg.node().cloneNode(true);

            // Ajouter un fond blanc
            const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
            rect.setAttribute('width', '100%');
            rect.setAttribute('height', '100%');
            rect.setAttribute('fill', 'white');
            svgClone.insertBefore(rect, svgClone.firstChild);

            // Ajouter des styles incorporés
            const styleElement = document.createElement('style');
            const cssRules = Array.from(document.styleSheets)
                .filter(sheet => !sheet.href || sheet.href.startsWith(window.location.origin))
                .flatMap(sheet => {
                    try {
                        return Array.from(sheet.cssRules);
                    } catch (e) {
                        console.warn('Impossible d\'accéder aux règles CSS d\'une feuille de style:', e);
                        return [];
                    }
                })
                .filter(rule => rule.selectorText && (
                    rule.selectorText.includes('.node') ||
                    rule.selectorText.includes('.link') ||
                    rule.selectorText.includes('circle') ||
                    rule.selectorText.includes('text')
                ))
                .map(rule => rule.cssText)
                .join('\n');

            styleElement.textContent = cssRules;
            svgClone.insertBefore(styleElement, svgClone.firstChild.nextSibling);

            // Sérialiser le SVG
            const svgData = new XMLSerializer().serializeToString(svgClone);

            // Ajouter un en-tête XML pour s'assurer que l'image sera correctement rendue
            const svgBlob = new Blob([
                '<?xml version="1.0" standalone="no"?>',
                '<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">',
                svgData
            ], {type: 'image/svg+xml;charset=utf-8'});

            const url = URL.createObjectURL(svgBlob);

            const img = new Image();
            img.onload = function () {
                const canvas = document.createElement('canvas');
                canvas.width = width;
                canvas.height = height;

                const ctx = canvas.getContext('2d');
                ctx.fillStyle = '#ffffff';
                ctx.fillRect(0, 0, width, height);
                ctx.drawImage(img, 0, 0);

                try {
                    const pngUrl = canvas.toDataURL('image/png');
                    const link = document.createElement('a');
                    link.href = pngUrl;
                    link.download = `${currentMapping.id}-mapping.png`;
                    link.click();
                } catch (err) {
                    console.error("Erreur lors de la conversion en PNG:", err);
                    alert("Impossible de convertir en PNG. Essayez l'exportation SVG à la place.");
                }

                URL.revokeObjectURL(url);
            };

            img.onerror = function (err) {
                console.error("Erreur lors du chargement de l'image SVG:", err);
                alert("Impossible de charger l'image SVG pour la conversion en PNG. Essayez l'exportation SVG à la place.");
                URL.revokeObjectURL(url);
            };

            img.src = url;
        } catch (error) {
            console.error("Erreur lors de l'exportation PNG:", error);
            alert("Impossible d'exporter au format PNG: " + error.message);
        }

    }
    /**
     * Exporte le mapping au format DOT (Graphviz)
     */
    function exportDOT() {
        if (!currentMapping) return;

        console.log("Exportation au format DOT pour:", currentMapping.id);

        fetch(`${apiBaseUrl}/${currentMapping.id}/graphviz`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Erreur HTTP: ${response.status} - ${response.statusText}`);
                }
                return response.text();
            })
            .then(dotContent => {
                const blob = new Blob([dotContent], { type: 'text/plain' });
                const url = URL.createObjectURL(blob);

                const link = document.createElement('a');
                link.href = url;
                link.download = `${currentMapping.id}-mapping.dot`;
                link.click();

                URL.revokeObjectURL(url);
            })
            .catch(error => {
                console.error('Erreur lors de l\'exportation DOT:', error);
                alert('Impossible d\'exporter au format DOT: ' + error.message);
            });
    }

    /**
     * Exporte le mapping au format JSON (D3)
     */
    function exportJSON() {
        if (!currentMapping) return;

        console.log("Exportation au format JSON pour:", currentMapping.id);

        fetch(`${apiBaseUrl}/${currentMapping.id}/d3`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Erreur HTTP: ${response.status} - ${response.statusText}`);
                }
                return response.json();
            })
            .then(jsonContent => {
                const blob = new Blob([JSON.stringify(jsonContent, null, 2)], { type: 'application/json' });
                const url = URL.createObjectURL(blob);

                const link = document.createElement('a');
                link.href = url;
                link.download = `${currentMapping.id}-mapping.json`;
                link.click();

                URL.revokeObjectURL(url);
            })
            .catch(error => {
                console.error('Erreur lors de l\'exportation JSON:', error);
                alert('Impossible d\'exporter au format JSON: ' + error.message);
            });
    }

    /**
     * Affiche la modal d'édition du mapping
     */
    function showEditMappingModal(mapping) {
        const modalElement = document.getElementById('edit-mapping-modal');
        if (!modalElement) {
            console.error("Modal d'édition introuvable dans le DOM");
            return;
        }

        const form = document.getElementById('mapping-edit-form');
        if (!form) {
            console.error("Formulaire d'édition introuvable dans le DOM");
            return;
        }

        form.innerHTML = `
        <div class="mb-3">
            <label for="mapping-id" class="form-label">ID</label>
            <input type="text" class="form-control" id="mapping-id" value="${mapping.id}" readonly>
        </div>
        <div class="mb-3">
            <label for="mapping-source-type" class="form-label">Type Source</label>
            <input type="text" class="form-control" id="mapping-source-type" value="${mapping.sourceType}">
        </div>
        <div class="mb-3">
            <label for="mapping-target-type" class="form-label">Type Cible</label>
            <input type="text" class="form-control" id="mapping-target-type" value="${mapping.targetType}">
        </div>
        <div class="mb-3">
            <label for="mapping-priority" class="form-label">Priorité</label>
            <input type="number" class="form-control" id="mapping-priority" value="${mapping.priority}">
        </div>
    `;

        // Utiliser Bootstrap pour afficher la modal
        try {
            const modal = new bootstrap.Modal(modalElement);
            modal.show();
        } catch (error) {
            console.error("Erreur lors de l'ouverture de la modal:", error);
            alert("Impossible d'ouvrir la modal d'édition. Vérifiez que Bootstrap est correctement chargé.");
        }
    }

    /**
     * Affiche la modal d'ajout d'un champ
     */
    function showAddFieldModal(mapping) {
        const modalElement = document.getElementById('edit-mapping-modal');
        if (!modalElement) {
            console.error("Modal d'ajout de champ introuvable dans le DOM");
            return;
        }

        const form = document.getElementById('mapping-edit-form');
        if (!form) {
            console.error("Formulaire d'ajout de champ introuvable dans le DOM");
            return;
        }

        form.innerHTML = `
        <div class="mb-3">
            <label for="field-source-path" class="form-label">Chemin Source</label>
            <input type="text" class="form-control" id="field-source-path" placeholder="ex: user.firstName">
        </div>
        <div class="mb-3">
            <label for="field-target-path" class="form-label">Chemin Cible</label>
            <input type="text" class="form-control" id="field-target-path" placeholder="ex: client.prenom">
        </div>
        <div class="mb-3">
            <label for="field-transformer" class="form-label">Transformateur</label>
            <select class="form-select" id="field-transformer">
                <option value="">Aucun</option>
                <option value="ma.adria.bank.mapping.transformers.StringCastTransformer">StringCastTransformer</option>
                <option value="ma.adria.bank.mapping.transformers.UpperCaseTransformer">UpperCaseTransformer</option>
                <option value="ma.adria.bank.mapping.transformers.LowerCaseTransformer">LowerCaseTransformer</option>
                <option value="ma.adria.bank.mapping.transformers.DateFormatTransformer">DateFormatTransformer</option>
                <option value="ma.adria.bank.mapping.transformers.EnumMappingTransformer">EnumMappingTransformer</option>
            </select>
        </div>
        <div class="mb-3">
            <label for="field-condition" class="form-label">Condition</label>
            <select class="form-select" id="field-condition">
                <option value="">Aucune</option>
                <option value="ma.adria.bank.mapping.core.definition.NotNullCondition">NotNullCondition</option>
            </select>
        </div>
        <div class="mb-3">
            <label for="field-name" class="form-label">Nom (optionnel)</label>
            <input type="text" class="form-control" id="field-name" placeholder="Nom du champ pour référence">
            <small class="form-text text-muted">Un nom peut être utilisé pour référencer ce champ dans d'autres mappings</small>
        </div>
        <div class="mb-3">
            <label for="field-constant" class="form-label">Valeur constante (optionnel)</label>
            <input type="text" class="form-control" id="field-constant" placeholder="Valeur constante">
            <small class="form-text text-muted">Si définie, cette valeur sera utilisée au lieu du chemin source</small>
        </div>
    `;

        // Utiliser Bootstrap pour afficher la modal
        try {
            const modal = new bootstrap.Modal(modalElement);
            modal.show();
        } catch (error) {
            console.error("Erreur lors de l'ouverture de la modal:", error);
            alert("Impossible d'ouvrir la modal d'ajout. Vérifiez que Bootstrap est correctement chargé.");
        }
    }

    /**
     * Affiche la modal d'édition d'un champ
     */
    function showEditFieldModal(mapping, fieldMapping, index) {
        const modalElement = document.getElementById('edit-mapping-modal');
        if (!modalElement) {
            console.error("Modal d'édition de champ introuvable dans le DOM");
            return;
        }

        const form = document.getElementById('mapping-edit-form');
        if (!form) {
            console.error("Formulaire d'édition de champ introuvable dans le DOM");
            return;
        }

        form.innerHTML = `
        <input type="hidden" id="field-index" value="${index}">
        <div class="mb-3">
            <label for="field-source-path" class="form-label">Chemin Source</label>
            <input type="text" class="form-control" id="field-source-path" value="${fieldMapping.sourcePath || ''}">
        </div>
        <div class="mb-3">
            <label for="field-target-path" class="form-label">Chemin Cible</label>
            <input type="text" class="form-control" id="field-target-path" value="${fieldMapping.targetPath || ''}">
        </div>
        <div class="mb-3">
            <label for="field-transformer" class="form-label">Transformateur</label>
            <select class="form-select" id="field-transformer">
                <option value="">Aucun</option>
                <option value="ma.adria.bank.mapping.transformers.StringCastTransformer" ${fieldMapping.transformer && fieldMapping.transformer['@class'] === 'ma.adria.bank.mapping.transformers.StringCastTransformer' ? 'selected' : ''}>StringCastTransformer</option>
                <option value="ma.adria.bank.mapping.transformers.UpperCaseTransformer" ${fieldMapping.transformer && fieldMapping.transformer['@class'] === 'ma.adria.bank.mapping.transformers.UpperCaseTransformer' ? 'selected' : ''}>UpperCaseTransformer</option>
                <option value="ma.adria.bank.mapping.transformers.LowerCaseTransformer" ${fieldMapping.transformer && fieldMapping.transformer['@class'] === 'ma.adria.bank.mapping.transformers.LowerCaseTransformer' ? 'selected' : ''}>LowerCaseTransformer</option>
                <option value="ma.adria.bank.mapping.transformers.DateFormatTransformer" ${fieldMapping.transformer && fieldMapping.transformer['@class'] === 'ma.adria.bank.mapping.transformers.DateFormatTransformer' ? 'selected' : ''}>DateFormatTransformer</option>
                <option value="ma.adria.bank.mapping.transformers.EnumMappingTransformer" ${fieldMapping.transformer && fieldMapping.transformer['@class'] === 'ma.adria.bank.mapping.transformers.EnumMappingTransformer' ? 'selected' : ''}>EnumMappingTransformer</option>
            </select>
        </div>
        <div class="mb-3">
            <label for="field-condition" class="form-label">Condition</label>
            <select class="form-select" id="field-condition">
                <option value="">Aucune</option>
                <option value="ma.adria.bank.mapping.core.definition.NotNullCondition" ${fieldMapping.condition && fieldMapping.condition['@class'] === 'ma.adria.bank.mapping.core.definition.NotNullCondition' ? 'selected' : ''}>NotNullCondition</option>
            </select>
        </div>
        <div class="mb-3">
            <label for="field-name" class="form-label">Nom (optionnel)</label>
            <input type="text" class="form-control" id="field-name" value="${fieldMapping.name || ''}" placeholder="Nom du champ pour référence">
            <small class="form-text text-muted">Un nom peut être utilisé pour référencer ce champ dans d'autres mappings</small>
        </div>
        <div class="mb-3">
            <label for="field-constant" class="form-label">Valeur constante (optionnel)</label>
            <input type="text" class="form-control" id="field-constant" value="${fieldMapping.constant !== undefined ? fieldMapping.constant : ''}" placeholder="Valeur constante">
            <small class="form-text text-muted">Si définie, cette valeur sera utilisée au lieu du chemin source</small>
        </div>
        <div class="mb-3">
            <div class="form-check">
                <input class="form-check-input" type="checkbox" id="delete-field">
                <label class="form-check-label text-danger" for="delete-field">
                    Supprimer ce champ
                </label>
            </div>
        </div>
    `;

        // Utiliser Bootstrap pour afficher la modal
        try {
            const modal = new bootstrap.Modal(modalElement);
            modal.show();
        } catch (error) {
            console.error("Erreur lors de l'ouverture de la modal:", error);
            alert("Impossible d'ouvrir la modal d'édition. Vérifiez que Bootstrap est correctement chargé.");

            // Fallback si Bootstrap n'est pas disponible
            modalElement.style.display = 'block';
        }
    }

    /**
     * Enregistre les modifications apportées au mapping
     */
    function saveMapping() {
        // Vérifier que la modal est ouverte
        const modalElement = document.getElementById('edit-mapping-modal');
        if (!modalElement) {
            console.error("Modal d'édition introuvable dans le DOM");
            return;
        }

        // Déterminer le type d'édition (mapping entier, ajout de champ, édition de champ)
        const fieldIndex = document.getElementById('field-index');

        if (fieldIndex) {
            // Édition d'un champ existant
            const index = parseInt(fieldIndex.value, 10);
            const deleteField = document.getElementById('delete-field') && document.getElementById('delete-field').checked;

            if (deleteField) {
                // Supprimer un champ
                if (confirm(`Êtes-vous sûr de vouloir supprimer ce champ du mapping "${currentMapping.id}" ?`)) {
                    console.log(`Suppression du champ à l'index ${index} (simulée)`);
                    alert(`Le champ serait supprimé du mapping "${currentMapping.id}" (fonctionnalité non implémentée)`);
                }
            } else {
                // Mettre à jour un champ
                const sourcePath = document.getElementById('field-source-path').value;
                const targetPath = document.getElementById('field-target-path').value;
                const transformerClass = document.getElementById('field-transformer').value;
                const conditionClass = document.getElementById('field-condition').value;
                const name = document.getElementById('field-name') ? document.getElementById('field-name').value : '';
                const constant = document.getElementById('field-constant') ? document.getElementById('field-constant').value : '';

                console.log(`Mise à jour du champ à l'index ${index} (simulée):`, {
                    sourcePath,
                    targetPath,
                    transformerClass,
                    conditionClass,
                    name,
                    constant
                });

                alert(`Le champ serait mis à jour dans le mapping "${currentMapping.id}" (fonctionnalité non implémentée)`);
            }
        } else if (document.getElementById('field-source-path') && document.getElementById('field-target-path')) {
            // Ajout d'un nouveau champ
            const sourcePath = document.getElementById('field-source-path').value;
            const targetPath = document.getElementById('field-target-path').value;
            const transformerClass = document.getElementById('field-transformer').value;
            const conditionClass = document.getElementById('field-condition').value;
            const name = document.getElementById('field-name') ? document.getElementById('field-name').value : '';
            const constant = document.getElementById('field-constant') ? document.getElementById('field-constant').value : '';

            console.log(`Ajout d'un nouveau champ (simulé):`, {
                sourcePath,
                targetPath,
                transformerClass,
                conditionClass,
                name,
                constant
            });

            alert(`Un nouveau champ serait ajouté au mapping "${currentMapping.id}" (fonctionnalité non implémentée)`);
        } else {
            // Édition du mapping entier
            const sourceType = document.getElementById('mapping-source-type').value;
            const targetType = document.getElementById('mapping-target-type').value;
            const priority = parseInt(document.getElementById('mapping-priority').value, 10);

            console.log(`Mise à jour du mapping (simulée):`, {
                id: currentMapping.id,
                sourceType,
                targetType,
                priority
            });

            alert(`Le mapping "${currentMapping.id}" serait mis à jour (fonctionnalité non implémentée)`);
        }

        // Fermer la modal
        try {
            const modal = bootstrap.Modal.getInstance(modalElement);
            if (modal) {
                modal.hide();
            } else {
                // Fallback si l'instance bootstrap n'est pas accessible
                modalElement.classList.remove('show');
                modalElement.style.display = 'none';
                document.body.classList.remove('modal-open');
                const backdrop = document.querySelector('.modal-backdrop');
                if (backdrop) {
                    backdrop.remove();
                }
            }
        } catch (error) {
            console.error("Erreur lors de la fermeture de la modal:", error);
            // Fallback manuel
            modalElement.style.display = 'none';
        }
    }

    /**
     * Extrait le nom simple d'une classe à partir de son nom complet
     */
    function getSimpleClassName(fullClassName) {
        if (!fullClassName) return '';

        const lastDot = fullClassName.lastIndexOf('.');
        if (lastDot !== -1) {
            return fullClassName.substring(lastDot + 1);
        }
        return fullClassName;
    }
})