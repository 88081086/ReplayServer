function elt(type, props, ...children) {
  let dom = document.createElement(type);
  if (props) Object.assign(dom, props);
  for (let child of children) {
    if (typeof child != "string") dom.appendChild(child);
    else dom.appendChild(document.createTextNode(child));
  }
  return dom;
}

var Chess = class Chess {
    constructor(init, movelist) {
        this.dom = elt("canvas", {style: "width=100%"});
        let gap = this.dom.width/10;
        this.dom.height = gap*13;
        this.ctx = this.dom.getContext("2d");
        this.mGap = gap;
        this.mBoardExtraX = gap;
        this.mBoardExtraY = gap*2;

        this.board = new Array(90);
        let INIT = [
        0x0400, 0x0301, 0x0202, 0x0103, 0x0004, 0x0105, 0x0206, 0x0307, 0x0408,
                0x0521,                                         0x0527,
        0x0630,         0x0632,         0x0634,         0x0636,         0x0638,
        0x8660,         0x8662,         0x8664,         0x8666,         0x8668,
                0x8571,                                         0x8577,
        0x8490, 0x8391, 0x8292, 0x8193, 0x8094, 0x8195, 0x8296, 0x8397, 0x8498
        ];
        var r, c, i;
        for (r=0; r<=9; r++) {
            for (c=0; c<=8; c++) {
                this.board[r*9+c] = -1;
            }
        }
        for (i=0; i+4<=init.length; i+=4) {
            var p = parseInt("0x"+init.substring(i, i+4));
            r = (p>>4)&0xf;
            c = (p>>0)&0xf;
            this.board[r*9+c] = p>>8;
        }

        this.movelist = [];
        for (i=0; i<movelist.length; i+=4) {
            let c1 = movelist.charAt(i+0)-'0';
            let r1 = movelist.charAt(i+1)-'0';
            let c2 = movelist.charAt(i+2)-'0';
            let r2 = movelist.charAt(i+3)-'0';
            this.movelist.push({r1, c1, r2, c2});
        }
        this.move_pos = -1;
        this.eatlist = [];

        this.draw();
    }

    draw() {
        this.drawBoard();
        this.drawPiece();
        this.drawMark();
    }

    drawPiece() {
        let cx = this.ctx;
        let mGap = this.mGap;
        let mBoardExtraX = this.mBoardExtraX;
        let mBoardExtraY = this.mBoardExtraY;
        let mPieceSize = mGap*9/10;
        var x, y, color, cat;
        for (x=0; x<10; x++) {
            for (y=0; y<9; y++) {
                let v = this.board[x*9+y];
                if (v == -1) continue;
                color = (v>>7);
                cat = v&0x7;
                if (v&0x40) cat = -1;

                var textColor = "white";
                var bgColor = "black";
                if (color == 1) {
                    bgColor = "red";
                }

                cx.textAlign="center";
                //cx.textBaseLine="middle";
                cx.font=""+mGap/2+"px sans-serif";
                if (color == 0) {
                    cx.beginPath();
                    cx.arc(mBoardExtraX+y*mGap, mBoardExtraY+x*mGap, mPieceSize/2, 0, 7);
                    cx.fillStyle="black";
                    cx.fill();
                    if (cat != -1) {
                        cx.fillStyle="white";
                        let CAT = ["\u5c07", "\u4ed5", "\u8c61", "\u99ac", "\u8eca", "\u7832", "\u5352"];
                        cx.fillText(CAT[cat], mBoardExtraX+y*mGap, mBoardExtraY+x*mGap+mGap/5);
                    }
                } else {
                    cx.beginPath();
                    cx.arc(mBoardExtraX+y*mGap, mBoardExtraY+x*mGap, mPieceSize/2, 0, 7);
                    cx.fillStyle="red";
                    cx.fill();
                    if (cat != -1) {
                        cx.fillStyle="white";
                        let CAT = ["\u5c06", "\u58eb", "\u76f8", "\u9a6c", "\u8f66", "\u70ae", "\u5175"];
                        cx.fillText(CAT[cat], mBoardExtraX+y*mGap, mBoardExtraY+x*mGap+mGap/5);
                    }
                }
            }
        }
        
        var r_eat = [];
        var b_eat = [];
        for (let {eat} of this.eatlist) {
            if (eat == -1) continue;
            if (eat&0x80) {
                /* red eaten */
                var cat = eat&0x7;
                if (eat&0x40) {
                    cat = (eat>>3)&0x7;
                }
                for (i=0; i<r_eat.length; i++) {
                    if (r_eat[i].cat == cat) {
                        r_eat[i].num++;
                        break;
                    }
                }
                if (i == r_eat.length) {
                    r_eat.push({cat, num:1});
                }
            } else {
                /* black eaten */
                var cat = eat&0x7;
                if (eat&0x40) {
                    cat = (eat>>3)&0x7;
                }
                for (i=0; i<b_eat.length; i++) {
                    if (b_eat[i].cat == cat) {
                        b_eat[i].num++;
                        break;
                    }
                }
                if (i == b_eat.length) {
                    b_eat.push({cat, num:1});
                }
            }
        }
        var i = 0;
        for (i=0; i<r_eat.length; i++) {
            cx.beginPath();
            cx.arc(mBoardExtraX+i*mGap, mBoardExtraY-mGap, mPieceSize/2, 0, 7);
            cx.fillStyle="red";
            cx.fill();
            let cat = r_eat[i].cat;
            let num = r_eat[i].num;
            cx.fillStyle="white";
            cx.font=""+mGap/2+"px sans-serif";
            let CAT = ["\u5c06", "\u58eb", "\u76f8", "\u9a6c", "\u8f66", "\u70ae", "\u5175"];
            cx.fillText(CAT[cat], mBoardExtraX+i*mGap, mBoardExtraY-mGap+mGap/5);

            cx.beginPath();
            cx.arc(mBoardExtraX+i*mGap+mPieceSize/4, mBoardExtraY-mGap-mPieceSize/4, mPieceSize/6, 0, 7);
            cx.fillStyle="black";
            cx.fill();
            cx.fillStyle="white";
            cx.font=""+mGap/4+"px sans-serif";
            cx.fillText(""+num, mBoardExtraX+i*mGap+mPieceSize/4, mBoardExtraY-mGap-mPieceSize/4+mGap/10);
        }

        for (i=0; i<b_eat.length; i++) {
            cx.beginPath();
            cx.arc(mBoardExtraX+(8-i)*mGap, mBoardExtraY+mGap*10, mPieceSize/2, 0, 7);
            cx.fillStyle="black";
            cx.fill();
            let cat = b_eat[i].cat;
            let num = b_eat[i].num;
            cx.fillStyle="white";
            cx.font=""+mGap/2+"px sans-serif";
            let CAT = ["\u5c06", "\u58eb", "\u76f8", "\u9a6c", "\u8f66", "\u70ae", "\u5175"];
            cx.fillText(CAT[cat], mBoardExtraX+(8-i)*mGap, mBoardExtraY+mGap*10+mGap/5);

            cx.beginPath();
            cx.arc(mBoardExtraX+(8-i)*mGap+mPieceSize/4, mBoardExtraY+mGap*10-mPieceSize/4, mPieceSize/6, 0, 7);
            cx.fillStyle="red";
            cx.fill();
            cx.fillStyle="white";
            cx.font=""+mGap/4+"px sans-serif";
            cx.fillText(""+num, mBoardExtraX+(8-i)*mGap+mPieceSize/4, mBoardExtraY+mGap*10-mPieceSize/4+mGap/10);
        }
    }

    drawBoard() {
        let cx = this.ctx;
        let mGap = this.mGap;
        let mBoardExtraX = this.mBoardExtraX;
        let mBoardExtraY = this.mBoardExtraY;

        let unit = mGap/24;
        let strokeWidth = unit*2+1;
        let temp = unit+strokeWidth;
        let extraX = mBoardExtraX-temp;
        let extraY = mBoardExtraY-temp;

        cx.fillStyle = "#ffffff";
        cx.fillRect(0, 0, mGap*8+mBoardExtraX*2, mGap*9+mBoardExtraY*2);

        cx.strokeStyle = "black";
        cx.lineWidth = strokeWidth;
        cx.strokeRect(extraX, extraY, mGap*8+temp*2, mGap*9+temp*2);

        cx.lineWidth = 1;
        cx.strokeRect(mBoardExtraX, mBoardExtraY, mGap*8, mGap*9);

        /* grid */
        var x = 0, y = 0;
        var i = 0;
        for (y = mGap, i=1; i<9; y += mGap, i++)
            this.drawLine(0, y, mGap*8, y);
        for (x = mGap, i=1; i<8; x += mGap, i++) {
            this.drawLine(x, 0, x, mGap*4);
            this.drawLine(x, mGap*5, x, mGap*9);
        }

        /* x */
        this.drawLine(mGap*3, 0,      mGap*5, mGap*2);
        this.drawLine(mGap*3, mGap*2, mGap*5, 0);
        this.drawLine(mGap*3, mGap*7, mGap*5, mGap*9);
        this.drawLine(mGap*3, mGap*9, mGap*5, mGap*7);

        /* cornor */
        this.drawConor(2, 1);
        this.drawConor(2, 7);
        this.drawConor(3, 0);
        this.drawConor(3, 2);
        this.drawConor(3, 4);
        this.drawConor(3, 6);
        this.drawConor(3, 8);
        this.drawConor(6, 0);
        this.drawConor(6, 2);
        this.drawConor(6, 4);
        this.drawConor(6, 6);
        this.drawConor(6, 8);
        this.drawConor(7, 1);
        this.drawConor(7, 7);
    }

    drawMark() {
        if (this.move_pos == -1) return;

        let cx = this.ctx;
        let mGap = this.mGap;
        let mBoardExtraX = this.mBoardExtraX;
        let mBoardExtraY = this.mBoardExtraY;

        cx.strokeStyle = "Green";
        cx.lineWidth = 1;
        let {r1, c1, r2, c2} = this.movelist[this.move_pos];
        let x1 = mBoardExtraX+c1*mGap;
        let y1 = mBoardExtraY+r1*mGap;
        let x2 = mBoardExtraX+c2*mGap;
        let y2 = mBoardExtraY+r2*mGap;

        cx.beginPath();
        cx.moveTo(x1+mGap/2, y1-mGap/2+mGap/4);
        cx.lineTo(x1+mGap/2, y1-mGap/2);
        cx.lineTo(x1+mGap/2-mGap/4, y1-mGap/2);
        cx.stroke();

        cx.moveTo(x1-mGap/2+mGap/4, y1-mGap/2);
        cx.lineTo(x1-mGap/2, y1-mGap/2);
        cx.lineTo(x1-mGap/2, y1-mGap/2+mGap/4);
        cx.stroke();

        cx.moveTo(x1-mGap/2, y1+mGap/2-mGap/4);
        cx.lineTo(x1-mGap/2, y1+mGap/2);
        cx.lineTo(x1-mGap/2+mGap/4, y1+mGap/2);
        cx.stroke();

        cx.moveTo(x1+mGap/2-mGap/4, y1+mGap/2);
        cx.lineTo(x1+mGap/2, y1+mGap/2);
        cx.lineTo(x1+mGap/2, y1+mGap/2-mGap/4);
        cx.stroke();

        cx.beginPath();
        cx.moveTo(x2+mGap/2, y2-mGap/2+mGap/4);
        cx.lineTo(x2+mGap/2, y2-mGap/2);
        cx.lineTo(x2+mGap/2-mGap/4, y2-mGap/2);
        cx.stroke();

        cx.moveTo(x2-mGap/2+mGap/4, y2-mGap/2);
        cx.lineTo(x2-mGap/2, y2-mGap/2);
        cx.lineTo(x2-mGap/2, y2-mGap/2+mGap/4);
        cx.stroke();

        cx.moveTo(x2-mGap/2, y2+mGap/2-mGap/4);
        cx.lineTo(x2-mGap/2, y2+mGap/2);
        cx.lineTo(x2-mGap/2+mGap/4, y2+mGap/2);
        cx.stroke();

        cx.moveTo(x2+mGap/2-mGap/4, y2+mGap/2);
        cx.lineTo(x2+mGap/2, y2+mGap/2);
        cx.lineTo(x2+mGap/2, y2+mGap/2-mGap/4);
        cx.stroke();
    }

    drawLine(x1, y1, x2, y2) {
        let cx = this.ctx;
        let mGap = this.mGap;
        let mBoardExtraX = this.mBoardExtraX;
        let mBoardExtraY = this.mBoardExtraY;
        cx.beginPath();
        cx.moveTo(x1+mBoardExtraX, y1+mBoardExtraY);
        cx.lineTo(x2+mBoardExtraX, y2+mBoardExtraY);
        cx.stroke();
    }

    drawConor(i, j)
    {
        let ctx = this.ctx;
        let mGap = this.mGap;
        let mBoardExtraX = this.mBoardExtraX;
        let mBoardExtraY = this.mBoardExtraY;

        let x = mBoardExtraX+j*mGap;
        let y = mBoardExtraY+i*mGap;
        let gap = mGap/12;
        let len = mGap/4;

        if (j > 0) {
            /* 2nd space */
            ctx.beginPath();
            ctx.moveTo(x-len, y-gap);
            ctx.lineTo(x-gap, y-gap);
            ctx.lineTo(x-gap, y-len);
            ctx.stroke();

            /* 3rd space */
            ctx.beginPath();
            ctx.moveTo(x-len, y+gap);
            ctx.lineTo(x-gap, y+gap);
            ctx.lineTo(x-gap, y+len);
            ctx.stroke();
        }

        if (j<8) {
            /* 4th space */
            ctx.beginPath();
            ctx.moveTo(x+len, y+gap);
            ctx.lineTo(x+gap, y+gap);
            ctx.lineTo(x+gap, y+len);
            ctx.stroke();

            /* 1st space */
            ctx.beginPath();
            ctx.moveTo(x+len, y-gap);
            ctx.lineTo(x+gap, y-gap);
            ctx.lineTo(x+gap, y-len);
            ctx.stroke();
        }
    }

    prev() {
        if (this.move_pos >= 0) {
            let {eat, jie} = this.eatlist.pop();
            let {r1, c1, r2, c2} = this.movelist[this.move_pos];
            this.board[r1*9+c1] = this.board[r2*9+c2];
            if (jie) {
                var src = this.board[r2*9+c2];
                var color = (src&0x80);
                var cur = (src&0x7);
                var alt = ((src>>3)&0x7);
                this.board[r1*9+c1] = (color|jie<<6|cur<<3|alt);
            }
            this.board[r2*9+c2] = eat;
            this.move_pos--;
            return 0;
        } else {
            return -1;
        }
    }

    next() {
        if (this.move_pos < this.movelist.length-1) {
            let {r1, c1, r2, c2} = this.movelist[++this.move_pos];
            let eat = this.board[r2*9+c2];
            var src = this.board[r1*9+c1];
            var new_src = src;
            let jie = (src>>6)&0x1;
            if (jie) {
                var color = (src&0x80);
                var cur = (src&0x7);
                var alt = ((src>>3)&0x7);
                new_src = (color|cur<<3|alt);
            }
            this.board[r2*9+c2] = new_src;
            this.board[r1*9+c1] = -1;
            this.eatlist.push({eat, jie});
            return 0;
        } else {
            return -1;
        }
    }

    first() {
        while (this.prev() == 0);
    }

    last() {
        while (this.next() == 0);
    }

    isFirst() {
        return this.move_pos == -1;
    }

    isLast() {
        return this.move_pos == this.movelist.length-1;
    }
}

var ChessGame = class ChessGame {
    constructor(config, {dispatch}) {
        this.chess = new Chess(config.init, config.movelist);
        let controls = [FirstButton, PrevButton, LabelButton, NextButton, LastButton];
        this.controls = controls.map(
            Control => new Control(this.chess, dispatch));
        var info = [];
        var x;
        for (x in config) {
            if (x == "red") info.push(elt("tr", {}, elt("td", {}, "??????: "+config[x])));
            if (x == "black") info.push(elt("tr", {}, elt("td", {}, "??????: "+config[x])));
            if (x == "date") info.push(elt("tr", {}, elt("td", {}, "??????: "+config[x])));
            if (x == "site") info.push(elt("tr", {}, elt("td", {}, "??????: "+config[x])));
            if (x == "event") info.push(elt("tr", {}, elt("td", {}, "??????: "+config[x])));
        }
        /*
        this.dom = elt("div", {}, elt("table", {}, ...info), elt("br", {}), this.chess.dom, 
            elt("table", {style: "background-color: #cccccc; width: "+this.chess.mGap*10}, elt("tr", {}, ...this.controls.reduce(
                (a, c) => a.concat(elt("td", {style: "text-align: center;"}, c.dom)), []))));
                */
        this.dom = elt("table", {style: "width: "+this.chess.mGap*10}, ...info, elt("tr", {}, elt("td", {}, this.chess.dom)), 
            elt("tr", {}, elt("td", {}, 
            elt("table", {style: "width: 100%"}, elt("tr", {}, ...this.controls.reduce(
                (a, c) => a.concat(elt("td", {style: "text-align: center;"}, c.dom)), []))))));
    }

    syncState() {
        this.chess.draw();
        for (let ctrl of this.controls) ctrl.syncState(this.chess);
    }
}

var FirstButton = class FirstButton {
  constructor(chess, dispatch) {
    this.dom = elt("button", {
      onclick: () => {chess.first(); dispatch();},
      disabled: chess.isFirst(),
      style: "min-width: "+chess.mGap*3/2,
    }, "|<");
  }
  syncState(chess) {
    this.dom.disabled = chess.isFirst();
  }
}

var PrevButton = class PrevButton {
  constructor(chess, dispatch) {
    this.dom = elt("button", {
      onclick: () => {chess.prev(); dispatch();},
      disabled: chess.isFirst(),
      style: "min-width: "+chess.mGap*3/2,
    }, "<<");
  }
  syncState(chess) {
    this.dom.disabled = chess.isFirst();
  }
}

var LabelButton = class LabelButton {
  constructor(chess, dispatch) {
    var movelist = [];
    var i;
    for (i=0; i<=chess.movelist.length; i++) {
      movelist.push(elt("option", {value: i-1}, ""+i));
    }
    this.dom= elt("select", {
      onchange: () => {while (chess.move_pos < this.dom.value) chess.next(); while (chess.move_pos > this.dom.value) chess.prev(); dispatch();}
    }, ...movelist);
    this.syncState(chess);
  }
  syncState(chess) {
    this.dom.value = chess.move_pos;
  }
}

var NextButton = class NextButton {
  constructor(chess, dispatch) {
    this.dom = elt("button", {
      onclick: () => {chess.next(); dispatch();},
      disabled: chess.isLast(),
      style: "min-width: "+chess.mGap*3/2,
    }, ">>");
  }
  syncState(chess) {
    this.dom.disabled = chess.isLast();
  }
}

var LastButton = class LastButton {
  constructor(chess, dispatch) {
    this.dom = elt("button", {
      onclick: () => {chess.last(); dispatch();},
      disabled: chess.isLast(),
      style: "min-width: "+chess.mGap*3/2,
    }, ">|");
  }
  syncState(chess) {
    this.dom.disabled = chess.isLast();
  }
}

function startGame(config) {
    let app = new ChessGame(config, {dispatch() {
        app.syncState();
    }});
    return app.dom;
}
