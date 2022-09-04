"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;

var _react = _interopRequireDefault(require("react"));

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

class Button extends _react.default.Component {
  render() {
    return /*#__PURE__*/_react.default.createElement("button", {
      style: {
        border: '1px solid black',
        borderRadius: 10,
        padding: "5px 10px 5px 10px",
        cursor: "pointer",
        outline: "none"
      },
      onClick: this.props.function
    }, /*#__PURE__*/_react.default.createElement("span", {
      style: {
        fontSize: 14,
        fontWeight: 500
      }
    }, " ", this.props.text, " "));
  }

}

exports.default = Button;