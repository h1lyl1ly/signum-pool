// React
import { Fragment } from "react";

// Material ui
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";

// Styling
import styles from "./tradingView.module.css";

// Extra
import { isMobileOnly } from "react-device-detect";
import {
  POOLNameToUse,
  MINI_WIDGET_TRADING_LINKToUse,
  LARGE_WIDGET_TRADING_LINKToUse,
} from "../../utils/globalParameters";

// SEO
import { Helmet } from "react-helmet";

const TradingViewer = () => {
  return (
    <Fragment>
      {/* Basic SEO */}
      <Helmet>
        <title>{"Trading viewer • " + POOLNameToUse}</title>
      </Helmet>

      {/* First section */}
      <Grid
        container
        className={styles.firstSection}
        direction="column"
        justify="flex-start"
        alignItems="flex-start"
        component="section"
      >
        <Typography component="h1" variant="h4" align="center" gutterBottom>
          Trading Viewer
        </Typography>

        <Grid item container style={{ marginBottom: "1rem" }}>
          <iframe
            scrolling="no"
            allowtransparency="true"
            frameBorder="0"
            title="Mini widget"
            // Shortened link
            src={MINI_WIDGET_TRADING_LINKToUse}
            style={{
              boxSizing: "border-box",
              height: "calc(72px)",
              width: "100%",
            }}
          ></iframe>
        </Grid>

        <Grid item container style={{ marginBottom: "1rem" }}>
          <iframe
            scrolling="no"
            allowtransparency="true"
            frameBorder="0"
            title="Large widget"
            // Shortened link
            src={LARGE_WIDGET_TRADING_LINKToUse}
            style={{
              boxSizing: "border-box",
              height: isMobileOnly ? "400px" : "60vh",
              width: "100%",
            }}
          ></iframe>
        </Grid>
      </Grid>
    </Fragment>
  );
};

export default TradingViewer;
