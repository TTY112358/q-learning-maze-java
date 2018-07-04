var mapContaier = $('#map-container');
var yCount;
var xCount;
window.onload = function (ev) {
    console.log("hello");
    getMap().then(function (map) {
        yCount = map.length;
        xCount = map[0].length;
        mapContaier.css({'width': xCount * 30 + 'px', 'height': yCount * 30 + 'px'});
        for (var i = 0; i < yCount; i++) {
            var mapLine = map[i];
            var rowToAppend = $('<div class="map-row"></div>');
            for (var j = 0; j < xCount; j++) {
                var cell = mapLine[j];
                var cellToAppend = $('<div class="cell" id="cell-' + i + '-' + j + '"></div>');
                if (cell[0] === 1) {
                    cellToAppend.addClass('top');
                }
                if (cell[1] === 1) {
                    cellToAppend.addClass('right');
                }
                if (cell[2] === 1) {
                    cellToAppend.addClass('bottom');
                }
                if (cell[3] === 1) {
                    cellToAppend.addClass('left');
                }
                cellToAppend.on('click', function (x, y) {
                    return function () {
                        onCellClicked(x, y);
                    }
                }(j, i));
                rowToAppend.append(cellToAppend);
            }
            mapContaier.append(rowToAppend);
        }
    });
};

function getMap() {
    return new Promise(function (resolve) {
        $.get('/map', function (data) {
            var mapFromData = data.split("\n");
            var map = [];
            mapFromData.splice(0, 1);
            for (var lineIndex = 0; lineIndex < mapFromData.length; lineIndex++) {
                var line = mapFromData[lineIndex];
                if(line === ''){
                    continue;
                }
                var values = line.split("\t");
                if (!(parseInt(values[0]) in map)) {
                    map[parseInt(values[0])] = [];
                }
                map[parseInt(values[0])][parseInt(values[1])] = [parseInt(values[2]), parseInt(values[3]), parseInt(values[4]), parseInt(values[5])];
            }
            resolve(map);
        })
    })
}

function getPath(x, y) {
    return new Promise(function (resolve) {
        $.get('/path?x=' + x + '&y=' + y, function (data) {
            var moves = data.split("\n");
            var parsedMoves = [];
            var reversedMoves = [];
            for (var i = 0; i < moves.length; i++) {
                if(moves[i] === ''){
                    continue;
                }
                var moveX = moves[i].split('\t')[0];
                var moveY = moves[i].split('\t')[1];
                parsedMoves.push([parseInt(moveX), parseInt(moveY)]);
            }
            for(var i = 0; i < parsedMoves.length; i++){
                reversedMoves[parsedMoves.length - 1 - i] = parsedMoves[i];
            }
            resolve(reversedMoves);
        })
    })
}

var isTracing = false;

function onCellClicked(x, y) {
    if (isTracing) {
        return;
    }
    for (var i = 0; i < yCount; i++) {
        for (var j = 0; j < xCount; j++) {
            mapContaier.find('#cell-' + i + '-' + j).removeClass('trace').removeClass('target');
        }
    }
    var targetCell = mapContaier.find('#cell-' + y + '-' + x);
    targetCell.addClass('target');
    return getPath(x, y).then(function (path) {
        drawRemainingMoves(path);
    });
}

function drawRemainingMoves(remainingMoves) {
    var move = remainingMoves[0];
    var targetCell = mapContaier.find('#cell-' + move[1] + '-' + move[0]);
    targetCell.addClass('trace');
    remainingMoves.splice(0, 1);
    if (remainingMoves.length !== 0) {
        window.setTimeout(drawRemainingMoves, 100, remainingMoves)
    } else {
        isTracing = false;
    }
}