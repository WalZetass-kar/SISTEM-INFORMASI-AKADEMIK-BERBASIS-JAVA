const DEFAULT_PAGE = 1;
const DEFAULT_LIMIT = 10;
const MAX_LIMIT = 100;

const toPositiveInteger = (value, fallback, max = Number.MAX_SAFE_INTEGER) => {
  const number = Number.parseInt(value, 10);
  if (!Number.isFinite(number) || number < 1) {
    return fallback;
  }
  return Math.min(number, max);
};

const normalizePagination = ({ page = DEFAULT_PAGE, limit = DEFAULT_LIMIT } = {}) => {
  const normalizedPage = toPositiveInteger(page, DEFAULT_PAGE);
  const normalizedLimit = toPositiveInteger(limit, DEFAULT_LIMIT, MAX_LIMIT);

  return {
    page: normalizedPage,
    limit: normalizedLimit,
    offset: (normalizedPage - 1) * normalizedLimit
  };
};

module.exports = {
  normalizePagination
};
