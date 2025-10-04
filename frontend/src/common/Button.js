import React from 'react';

export default class Button extends React.Component {

    render() {
        return (
            <button style={{
                border: '1px solid black',
                borderRadius: 10,
                padding: "5px 10px 5px 10px",
                cursor: "pointer",
                outline: "none",
            }} onClick={this.props.function}>
                <span  style={{
                    fontSize: 14,
                    fontWeight: 500,
                }}> {this.props.text} </span>
            </button>
        )

    }
}
